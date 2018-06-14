package com.releaseit.cryptoprices.list

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
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
import com.releaseit.cryptoprices.utils.cast
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import com.releaseit.cryptoprices.utils.inflate
import com.releaseit.cryptoprices.utils.showToast
import dagger.Module
import dagger.Provides
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_crypto_list.*
import kotlinx.android.synthetic.main.item_crypto.view.*
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

/**
 * STATE
 */
data class State(val items: List<Crypto>,
                 val error: Optional<StateError>,
                 val loading: Boolean,
                 val currency: Currency) {
  companion object {
    fun initial() = State(emptyList(), Optional.None(), true, Currency.USD)
  }
}

/**
 * EVENTS
 */
sealed class Event {
  data class DataLoaded(val items: List<Crypto>) : Event()
  data class Error(val error: StateError) : Event()
  data class CurrencyChanged(val currency: Currency) : Event()
  object ReloadData : Event()
}

/**
 * REDUCER
 */
fun State.Companion.reduce(state: State, event: Event) =
  when (event) {
    is Event.DataLoaded      -> state.copy(items = event.items, error = Optional.None(), loading = false)
    is Event.Error           -> state.copy(error = Optional.Some(event.error), loading = false)
    is Event.CurrencyChanged -> state.copy(currency = event.currency, loading = true)
    Event.ReloadData         -> state.copy(loading = true, error = Optional.None())
  }

/**
 * Other state stuff
 */
sealed class StateError {
  object NoInternet : StateError()
  object Unknown : StateError()
}

/**
 * FEEDBACKS
 */

private val CryptoRepository.feedback: SignalFeedback<State, Event>
  get() = reactSafe<State, Currency, Event>(
    query = {
      if (it.loading) return@reactSafe Optional.Some(it.currency)
      return@reactSafe Optional.None()
    },
    effects = {
      this.getCryptos(it, "${com.releaseit.cryptoprices.repository.CryptoRepository.LIMIT}")
        .toObservable()
        .map<Event> { Event.DataLoaded(it) }
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
class CryptoListFragmentModule {

  @Provides
  @PerFragment
  fun cryptoListViewModelFactory(cryptoRepository: CryptoRepository,
                                 prefs: Prefs): RxFeedbackViewModelFactory<State, Event> =
    RxFeedbackViewModelFactory(State.initial(),
                               { s, e -> State.reduce(s, e) },
                               listOf(cryptoRepository.feedback, prefs.feedback))
}

/**
 * VIEW MODEL STUFF
 */

private typealias CryptoListViewModel = RxFeedbackViewModel<State, Event>

private typealias CryptoListViewModelFactory = RxFeedbackViewModelFactory<State, Event>

/**
 * VIEW STUFF - Fragment
 */

class CryptoListFragment : DaggerFragment() {

  companion object {
    fun newInstance(): Fragment = CryptoListFragment()
  }

  @Inject
  lateinit var viewModelFactory: CryptoListViewModelFactory

  @Inject
  lateinit var navigator: Navigator

  private lateinit var viewModel: CryptoListViewModel

  private var disposable: CompositeDisposable = CompositeDisposable()

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel =
      ViewModelProviders.of(this, viewModelFactory)
        .get(RxFeedbackViewModel::class.java) as CryptoListViewModel
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
    inflater.inflate(R.layout.fragment_crypto_list, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cryptoListFragmentSwipeRefreshLayout.setOnRefreshListener { viewModel.event(Event.ReloadData) }
    cryptoListFragmentRecylerView.apply {
      layoutManager = LinearLayoutManager(context)
    }
    cryptoListFragmentToolbar.inflateMenu(R.menu.menu_main)
    cryptoListFragmentToolbar.setOnMenuItemClickListener {
      if (it.itemId == R.id.menu_action_settings) {
        navigator.navigateTo(Screen.Settings)
        return@setOnMenuItemClickListener true
      }
      return@setOnMenuItemClickListener false
    }
  }

  override fun onStart() {
    super.onStart()
    disposable.addAll(itemsDisposable(), loadingDisposable(), errorDisposable())
  }

  override fun onStop() {
    disposable.clear()
    super.onStop()
  }

  private fun itemsDisposable() =
    viewModel.state
      .map { it.items }
      .distinctUntilChanged()
      .drive { items ->
        cryptoListFragmentRecylerView.adapter = CryptoAdapter(
          { items[it].listItem },
          { items.count() },
          { navigator.navigateTo(Screen.CryptoDetails(items[it].id)) })
      }

  private fun loadingDisposable() =
    viewModel.state
      .map { it.loading }
      .distinctUntilChanged()
      .filter { it != cryptoListFragmentSwipeRefreshLayout.isRefreshing }
      .drive { cryptoListFragmentSwipeRefreshLayout.isRefreshing = it }

  private fun errorDisposable() =
    viewModel.state
      .map { it.error }
      .filter { it !is Optional.None }
      .cast(Optional.Some::class.java)
      .drive {
        when (it.data) {
          StateError.NoInternet -> context?.showToast(R.string.error_no_internet)
          StateError.Unknown    -> context?.showToast(R.string.error_unknown)
        }
      }
}

/**
 * Mapper from Crypto to CryptoListItem
 */
private val Crypto.listItem: CryptoListItem
  get() = CryptoListItem(rank, symbol, "$price ${currency.name}", "$percentChange24h%")

/**
 * List item
 */
data class CryptoListItem(val rank: String, val symbol: String, val price: String, val percentChange24h: String)

/**
 * Recyclerview adapter
 */
private class CryptoAdapter(private val itemProvider: (Int) -> CryptoListItem, private val itemCount: () -> Int,
                            private val clickListener: (Int) -> (Unit))
  : RecyclerView.Adapter<CryptoViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    CryptoViewHolder(parent.inflate(R.layout.item_crypto))

  override fun getItemCount() = itemCount()

  override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
    holder.bind(itemProvider(position), { clickListener(position) })
  }
}

private class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(item: CryptoListItem, clickListener: () -> Unit) {
    itemView.itemCryptoRoot.setOnClickListener { clickListener() }
    itemView.itemCryptoRank.text = item.rank
    itemView.itemCryptoSymbol.text = item.symbol
    itemView.itemCryptoPrice.text = item.price
    itemView.itemCryptoVolume24h.text = item.percentChange24h
  }
}
