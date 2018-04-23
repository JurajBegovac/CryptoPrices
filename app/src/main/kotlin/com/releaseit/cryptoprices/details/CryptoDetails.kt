package com.releaseit.cryptoprices.details

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.navigation.Navigator
import com.releaseit.cryptoprices.navigation.Screen
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.Currency
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.RxFeedbackViewModel
import com.releaseit.cryptoprices.utils.RxFeedbackViewModelFactory
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import com.releaseit.cryptoprices.utils.inflate
import com.releaseit.cryptoprices.utils.showToast
import dagger.Module
import dagger.Provides
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_crypto_details.*
import kotlinx.android.synthetic.main.item_crypto_details.view.*
import org.notests.rxfeedback.Optional
import org.notests.rxfeedback.SignalFeedback
import org.notests.rxfeedback.reactSafe
import org.notests.sharedsequence.Signal
import org.notests.sharedsequence.asSignal
import org.notests.sharedsequence.distinctUntilChanged
import org.notests.sharedsequence.drive
import org.notests.sharedsequence.filter
import org.notests.sharedsequence.just
import org.notests.sharedsequence.map
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Named

data class State(val crypto: Optional<Crypto>,
                 val error: Optional<StateError>,
                 val loading: Boolean,
                 val currency: Currency) {
  companion object {
    val initial = State(Optional.None(), Optional.None(), true, Currency.USD)
  }
}

sealed class StateError {
  object NoInternet : StateError()
  object Unknown : StateError()
}

sealed class Event {
  data class CryptoLoaded(val crypto: Crypto) : Event()
  data class Error(val error: StateError) : Event()
  data class CurrencyChanged(val currency: Currency) : Event()
  object ReloadData : Event()
}

/**
 * REDUCER
 */
fun State.Companion.reduce(state: State, event: Event) =
  when (event) {
    is Event.CryptoLoaded    -> state.copy(crypto = Optional.Some(event.crypto),
                                           error = Optional.None(),
                                           loading = false)
    is Event.Error           -> state.copy(error = Optional.Some(event.error), loading = false)
    is Event.CurrencyChanged -> state.copy(currency = event.currency, loading = true)
    Event.ReloadData         -> state.copy(loading = true, error = Optional.None())
  }

/**
 * FEEDBACKS
 */

private fun CryptoRepository.feedback(id: String): SignalFeedback<State, Event> =
  reactSafe<State, Currency, Event>(
    query = {
      if (it.loading) return@reactSafe Optional.Some(it.currency)
      return@reactSafe Optional.None()
    },
    effects = {
      this.getCrypto(id, it)
        .toObservable()
        .map<Event> { Event.CryptoLoaded(it) }
        .asSignal<Event> {
          if (it is UnknownHostException) return@asSignal Signal.just(Event.Error(StateError.NoInternet))
          return@asSignal Signal.just(Event.Error(StateError.Unknown))
        }
    })

private val Prefs.feedback: SignalFeedback<State, Event>
  get() = {
    this.currencyObservable
      .map<Event> { Event.CurrencyChanged(it) }
      .asSignal { Signal.just(Event.Error(StateError.Unknown)) }
  }

/**
 * DI - dagger
 */
@Module
class CryptoDetailsFragmentModule {

  @Provides
  @PerFragment
  @Named(CryptoDetailsFragment.KEY_ID)
  fun id(cryptoDetailsFragment: CryptoDetailsFragment): String =
    cryptoDetailsFragment.arguments.getString(CryptoDetailsFragment.KEY_ID)!!

  @Provides
  @PerFragment
  fun cryptoDetailsViewModelFactory(@Named(CryptoDetailsFragment.KEY_ID) id: String,
                                 cryptoRepository: CryptoRepository,
                                 prefs: Prefs): RxFeedbackViewModelFactory<State, Event> =
    RxFeedbackViewModelFactory(State.initial,
                               { s, e -> State.reduce(s, e) },
                               listOf(cryptoRepository.feedback(id), prefs.feedback))
}

/**
 * VIEW MODEL STUFF
 */

private typealias CryptoDetailsViewModel = RxFeedbackViewModel<State, Event>

private typealias CryptoDetailsViewModelFactory = RxFeedbackViewModelFactory<State, Event>

class CryptoDetailsFragment : DaggerFragment() {

  companion object {
    const val KEY_ID = "CryptoDetailsFragment:key:id"
    fun newInstance(id: String): Fragment {
      val cryptoDetailsFragment = CryptoDetailsFragment()
      val bundle = Bundle()
      bundle.putString(KEY_ID, id)
      cryptoDetailsFragment.arguments = bundle
      return cryptoDetailsFragment
    }
  }

  @Inject
  lateinit var viewModelFactory: CryptoDetailsViewModelFactory

  @Inject
  lateinit var navigator: Navigator

  private lateinit var viewModel: CryptoDetailsViewModel

  private var disposable: CompositeDisposable = CompositeDisposable()

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProviders.of(this, viewModelFactory)
      .get(RxFeedbackViewModel::class.java) as CryptoDetailsViewModel
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_crypto_details, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cryptoDetailsFragmentSwipeRefreshLayout.setOnRefreshListener { viewModel.event(Event.ReloadData) }
    cryptoDetailsFragmentRecylerView.apply {
      layoutManager = LinearLayoutManager(context)
    }
    cryptoDetailsFragmentToolbar.apply {
      setNavigationOnClickListener { navigator.navigateBack() }
      inflateMenu(R.menu.menu_main)
      setOnMenuItemClickListener {
        if (it.itemId == R.id.menu_action_settings) {
          navigator.navigateTo(Screen.Settings)
          return@setOnMenuItemClickListener true
        }
        return@setOnMenuItemClickListener false
      }
    }
  }

  override fun onStart() {
    super.onStart()
    disposable.addAll(cryptoDisposable(), loadingDisposable(), errorDisposable())
  }

  override fun onStop() {
    disposable.clear()
    super.onStop()
  }

  private fun cryptoDisposable() =
    viewModel.state
      .map { it.crypto }
      .filter { it is Optional.Some }
      .map { (it as Optional.Some).data }
      .distinctUntilChanged()
      .drive {
        cryptoDetailsFragmentToolbar.title = it.name
        cryptoDetailsFragmentRecylerView.adapter = CryptoAdapter(it.detailItems)
      }

  private fun loadingDisposable() =
    viewModel.state
      .map { it.loading }
      .distinctUntilChanged()
      .filter { it != cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing }
      .drive { cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing = it }

  private fun errorDisposable() =
    viewModel.state
      .map { it.error }
      .filter { it !is Optional.None }
      .map { (it as Optional.Some).data }
      .drive {
        when (it) {
          StateError.NoInternet -> context.showToast(R.string.error_no_internet)
          StateError.Unknown    -> context.showToast(R.string.error_unknown)
        }
      }

}

/**
 * Mapper from Crypto to CryptoListItem
 */
private val Crypto.detailItems: List<CryptoDetailItem>
  get() = listOf(
    CryptoDetailItem(R.string.crypto_detail_name, name),
    CryptoDetailItem(R.string.crypto_detail_rank, rank),
    CryptoDetailItem(R.string.crypto_detail_symbol, symbol),
    CryptoDetailItem(R.string.crypto_detail_price, "$price $currency"),
    CryptoDetailItem(R.string.crypto_detail_24h_volume, "$_24hVolume $currency"),
    CryptoDetailItem(R.string.crypto_detail_market_cap, "$marketCap $currency"),
    CryptoDetailItem(R.string.crypto_detail_price_btc, priceBtc),
    CryptoDetailItem(R.string.crypto_detail_1h_change, "$percentChange1h%"),
    CryptoDetailItem(R.string.crypto_detail_24h_change, "$percentChange24h%"),
    CryptoDetailItem(R.string.crypto_detail_7d_change, "$percentChange7d%"),
    CryptoDetailItem(R.string.crypto_detail_total_supply, totalSupply),
    CryptoDetailItem(R.string.crypto_detail_available_supply, availableSupply))

/**
 * List item
 */
private data class CryptoDetailItem(@StringRes val nameResId: Int, val value: String)

/**
 * Recyclerview adapter
 */
private class CryptoAdapter(private val items: List<CryptoDetailItem>)
  : RecyclerView.Adapter<CryptoDetailsViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    CryptoDetailsViewHolder(parent.inflate(R.layout.item_crypto_details))

  override fun getItemCount() = items.count()

  override fun onBindViewHolder(holder: CryptoDetailsViewHolder?, position: Int) {
    holder?.bind(items[position])
  }
}

private class CryptoDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(item: CryptoDetailItem) {
    itemView.itemCryptoDetailName.setText(item.nameResId)
    itemView.itemCryptoDetailValue.text = item.value
  }
}
