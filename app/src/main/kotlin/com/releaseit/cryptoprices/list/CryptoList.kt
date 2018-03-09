package com.releaseit.cryptoprices.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
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
import org.notests.sharedsequence.*
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * DI - dagger
 */
@Module
class CryptoListFragmentModule {

    @Provides
    @PerFragment
    fun cryptoListViewModelFactory(cryptoRepository: CryptoRepository, prefs: Prefs) =
            CryptoListViewModelFactory(cryptoRepository, prefs)
}

/**
 * STATE
 */
data class State(val items: List<Crypto>, val error: StateError?, val loading: Boolean, val currency: Currency,
                 val selectedItem: Crypto?) {
    companion object {
        fun initial() = State(emptyList(), null, true, Currency.USD, null)
    }
}

/**
 * EVENTS
 */
sealed class Event {
    data class DataLoaded(val items: List<Crypto>) : Event()
    data class Error(val error: StateError) : Event()
    data class CurrencyChanged(val currency: Currency) : Event()
    data class ItemSelected(val position: Int) : Event()
    object ItemDeselected : Event()
    object ReloadData : Event()
}

/**
 * REDUCER
 */
fun State.Companion.reduce(state: State, event: Event) =
        when (event) {
            is Event.DataLoaded -> state.copy(items = event.items, error = null, loading = false, selectedItem = null)
            is Event.Error -> state.copy(error = event.error, loading = false)
            is Event.CurrencyChanged -> state.copy(currency = event.currency, loading = true)
            Event.ReloadData -> state.copy(loading = true)
            is Event.ItemSelected -> state.copy(selectedItem = state.items[event.position])
            Event.ItemDeselected -> if (state.selectedItem == null) state else state.copy(selectedItem = null)
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

internal val CryptoRepository.feedback: SignalFeedback<State, Event>
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
                            if (it is UnknownHostException) return@asSignal org.notests.sharedsequence.Signal.just(Event.Error(StateError.NoInternet))
                            return@asSignal org.notests.sharedsequence.Signal.just(Event.Error(StateError.Unknown))
                        }
            })

internal val Prefs.feedback: SignalFeedback<State, Event>
    get() = {
        this.currencyObservable
                .map<Event> { Event.CurrencyChanged(it) }
                .asSignal { Signal.just(Event.Error(StateError.Unknown)) }
    }

/**
 * VIEW MODEL STUFF
 */

class CryptoListViewModelFactory(private val repository: CryptoRepository,
                                 private val prefs: Prefs) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoListViewModel::class.java)) {
            return CryptoListViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CryptoListViewModel(repository: CryptoRepository, prefs: Prefs) :
        RxFeedbackViewModel<State, Event>(State.initial(), { s, e -> State.reduce(s, e) }, listOf(repository.feedback, prefs.feedback))

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
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CryptoListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_crypto_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
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
        disposable.addAll(itemsDisposable(),
                loadingDisposable(),
                errorDisposable(),
                itemSelectedDisposable()
        )
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
                                { viewModel.event(Event.ItemSelected(it)) })
                    }

    private fun loadingDisposable() =
            viewModel.state
                    .map { it.loading }
                    .distinctUntilChanged()
                    .filter { it != cryptoListFragmentSwipeRefreshLayout.isRefreshing }
                    .drive { cryptoListFragmentSwipeRefreshLayout.isRefreshing = it }

    private fun errorDisposable() =
            viewModel.state
                    .map {
                        if (it.error == null) return@map Optional.None<StateError>()
                        return@map Optional.Some(it.error)
                    }
                    .filter { it !is Optional.None }
                    .map { (it as Optional.Some).data }
                    .drive {
                        when (it) {
                            StateError.NoInternet -> context.showToast(R.string.error_no_internet)
                            StateError.Unknown -> context.showToast(R.string.error_unknown)
                        }
                    }

    private fun itemSelectedDisposable() =
            viewModel.state
                    .map {
                        if (it.selectedItem == null) return@map Optional.None<Crypto>()
                        return@map Optional.Some(it.selectedItem)
                    }
                    .filter { it !is Optional.None }
                    .map { (it as Optional.Some).data }
                    .drive {
                        viewModel.event(Event.ItemDeselected)
                        navigator.navigateTo(Screen.CryptoDetails(it.id))
                    }
}

/**
 * Mapper from Crypto to CryptoListItem
 */
internal val Crypto.listItem: CryptoListItem
    get() = CryptoListItem(rank, symbol, "$price ${currency.name}", "$percentChange24h%")

/**
 * List item
 */
data class CryptoListItem(val rank: String, val symbol: String, val price: String, val percentChange24h: String)

/**
 * Recyclerview adapter
 */
internal class CryptoAdapter(private val itemProvider: (Int) -> CryptoListItem, private val itemCount: () -> Int,
                             private val clickListener: (Int) -> (Unit))
    : RecyclerView.Adapter<CryptoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CryptoViewHolder(parent.inflate(R.layout.item_crypto))

    override fun getItemCount() = itemCount()

    override fun onBindViewHolder(holder: CryptoViewHolder?, position: Int) {
        holder?.bind(itemProvider(position), { clickListener(position) })
    }
}

internal class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: CryptoListItem, clickListener: () -> Unit) {
        itemView.itemCryptoRoot.setOnClickListener { clickListener() }
        itemView.itemCryptoRank.text = item.rank
        itemView.itemCryptoSymbol.text = item.symbol
        itemView.itemCryptoPrice.text = item.price
        itemView.itemCryptoVolume24h.text = item.percentChange24h
    }
}
