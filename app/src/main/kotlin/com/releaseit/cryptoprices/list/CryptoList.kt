package com.releaseit.cryptoprices.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.navigation.Screen.CryptoDetails
import com.releaseit.cryptoprices.navigation.Screen.Settings
import com.releaseit.cryptoprices.navigation.navigation
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.CryptoRepository.Companion.LIMIT
import com.releaseit.cryptoprices.utils.ClickableListItem
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.RxFeedbackView
import com.releaseit.cryptoprices.utils.RxFeedbackViewModel
import com.releaseit.cryptoprices.utils.bindUI
import com.releaseit.cryptoprices.utils.cast
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import com.releaseit.cryptoprices.utils.inflate
import com.releaseit.cryptoprices.utils.showToast
import com.releaseit.cryptoprices.utils.viewModel
import dagger.Module
import dagger.Provides
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_crypto_list.*
import kotlinx.android.synthetic.main.item_crypto.view.*
import org.notests.rxfeedback.Bindings
import org.notests.rxfeedback.Optional
import org.notests.rxfeedback.SignalFeedback
import org.notests.rxfeedback.reactSafe
import org.notests.sharedsequence.Driver
import org.notests.sharedsequence.Signal
import org.notests.sharedsequence.asSignal
import org.notests.sharedsequence.distinctUntilChanged
import org.notests.sharedsequence.drive
import org.notests.sharedsequence.empty
import org.notests.sharedsequence.filter
import org.notests.sharedsequence.just
import org.notests.sharedsequence.map
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * STATE
 */
data class State(val items: List<Crypto>, val error: Optional<StateError>, val loading: Boolean) {
  companion object {
    fun initial() = State(emptyList(), Optional.None(), true)
  }
}

/**
 * EVENTS
 */
sealed class Event {
  data class DataLoaded(val items: List<Crypto>) : Event()
  data class Error(val error: StateError) : Event()
  object ReloadData : Event()
}

/**
 * REDUCER
 */
fun State.Companion.reduce(state: State, event: Event) =
  when (event) {
    is Event.DataLoaded -> state.copy(items = event.items, error = Optional.None(), loading = false)
    is Event.Error      -> state.copy(error = Optional.Some(event.error), loading = false)
    Event.ReloadData    -> state.copy(loading = true, error = Optional.None())
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
val Prefs.currencyFeedback: SignalFeedback<State, Event>
  get() = { _ ->
    currencyObservable
      .distinctUntilChanged()
      .map<Event> { Event.ReloadData }
      .asSignal { Signal.just(Event.Error(StateError.Unknown)) }
  }

fun loadingFeedback(cryptoRepository: CryptoRepository, prefs: Prefs): SignalFeedback<State, Event> =
  reactSafe<State, Int, Event>(
    query = {
      if (it.loading) Optional.Some(LIMIT)
      else Optional.None()
    },
    effects = { limit ->
      prefs.currencyObservable
        .distinctUntilChanged()
        .flatMapSingle { cryptoRepository.getCryptos(it, "$limit") }
        .map<Event> { Event.DataLoaded(it) }
        .asSignal<Event> {
          if (it is UnknownHostException) Signal.just(Event.Error(StateError.NoInternet))
          else Signal.just(Event.Error(StateError.Unknown))
        }
    })

/**
 * VIEW MODEL STUFF
 */

typealias CryptoListViewModel = RxFeedbackViewModel<State, Event>

interface CryptoListViewModelFactory : () -> CryptoListViewModel

/**
 * DI - dagger
 */
@Module
class CryptoListFragmentModule {

  @PerFragment
  @Provides
  fun cryptoListViewModelFactory(cryptoRepository: CryptoRepository,
                                 prefs: Prefs): CryptoListViewModelFactory =
    object : CryptoListViewModelFactory {
      override fun invoke() =
        CryptoListViewModel(State.initial(),
                            { s, e -> State.reduce(s, e) },
                            listOf(prefs.currencyFeedback, loadingFeedback(cryptoRepository, prefs)))
    }
}

/**
 * VIEW STUFF - Fragment
 */

interface CryptoListView : RxFeedbackView<State, Event> {
  val swipeToRefresh: SwipeRefreshLayout
  fun showItems(items: List<Crypto>)
  fun showLoading(loading: Boolean)
  fun showError(error: StateError)

  override fun invoke(state: Driver<State>): Bindings<Event> {
    val itemsDisposable =
      state
        .map { it.items }
        .drive { showItems(it) }

    val loadingDisposable = state.map { it.loading }
      .distinctUntilChanged()
      .drive { showLoading(it) }

    val errorDisposable = state
      .map { it.error }
      .filter { it !is Optional.None }
      .cast(Optional.Some::class.java)
      .map { it.data as StateError }
      .drive { showError(it) }

    val subscriptions = listOf(itemsDisposable, loadingDisposable, errorDisposable)
    val events = listOf(swipeToRefresh.reloadEvent)

    return Bindings.safe(subscriptions, events)
  }
}

val SwipeRefreshLayout.reloadEvent: Signal<Event>
  get() = RxSwipeRefreshLayout.refreshes(this).map<Event> { Event.ReloadData }.asSignal { Signal.empty() }

class CryptoListFragment : DaggerFragment(), CryptoListView {

  companion object {
    fun newInstance() = CryptoListFragment()
  }

  @Inject
  lateinit var viewModelFactory: CryptoListViewModelFactory

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    viewModel(viewModelFactory).bindUI(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.fragment_crypto_list, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cryptoListFragmentRecylerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = CryptoAdapter()
    }

    cryptoListFragmentToolbar.apply {
      inflateMenu(R.menu.menu_main)
      setOnMenuItemClickListener { menuItem ->
        menuItem.takeIf { it.itemId == R.id.menu_action_settings }?.let { navigation.to(Settings); true } ?: false
      }
    }
  }

  override val swipeToRefresh: SwipeRefreshLayout
    get() = cryptoListFragmentSwipeRefreshLayout

  override fun showItems(items: List<Crypto>) {
    (cryptoListFragmentRecylerView.adapter as? CryptoAdapter)?.submitList(items.map { crypto ->
      crypto.listItem {
        navigation.to(CryptoDetails(it.id))
      }
    })
  }

  override fun showLoading(loading: Boolean) {
    if (cryptoListFragmentSwipeRefreshLayout.isRefreshing != loading)
      cryptoListFragmentSwipeRefreshLayout.isRefreshing = loading
  }

  override fun showError(error: StateError) {
    context?.showToast(when (error) {
                         StateError.NoInternet -> R.string.error_no_internet
                         StateError.Unknown    -> R.string.error_unknown
                       })
  }
}

/**
 * Mapper from Crypto to CryptoListItem
 */
private fun Crypto.listItem(onClick: (CryptoListItem) -> Unit): CryptoListItem =
  CryptoListItem(id, rank, symbol, "$price ${currency.name}", "$percentChange24h%", onClick)

/**
 * List item
 */
data class CryptoListItem(val id: String, val rank: String, val symbol: String, val price: String, val
percentChange24h: String, override val onClick: (CryptoListItem) -> Unit) :
  ClickableListItem<CryptoListItem> {
  companion object {
    val DIFF = object : DiffUtil.ItemCallback<CryptoListItem>() {
      override fun areItemsTheSame(oldItem: CryptoListItem, newItem: CryptoListItem) = oldItem.id == newItem.id
      override fun areContentsTheSame(oldItem: CryptoListItem, newItem: CryptoListItem) = oldItem == newItem
    }
  }
}

/**
 * Recyclerview adapter
 */
private class CryptoAdapter : ListAdapter<CryptoListItem, CryptoViewHolder>(CryptoListItem.DIFF) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder =
    CryptoViewHolder(parent.inflate(R.layout.item_crypto))

  override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

private class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(item: CryptoListItem) {
    itemView.itemCryptoRoot.setOnClickListener { item.onClick(item) }
    itemView.itemCryptoRank.text = item.rank
    itemView.itemCryptoSymbol.text = item.symbol
    itemView.itemCryptoPrice.text = item.price
    itemView.itemCryptoVolume24h.text = item.percentChange24h
  }
}
