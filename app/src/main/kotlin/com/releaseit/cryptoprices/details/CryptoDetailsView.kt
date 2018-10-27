package com.releaseit.cryptoprices.details

import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.utils.RxFeedbackView
import org.notests.rxfeedback.Bindings
import org.notests.sharedsequence.Driver
import org.notests.sharedsequence.Signal
import org.notests.sharedsequence.asSignal
import org.notests.sharedsequence.distinctUntilChanged
import org.notests.sharedsequence.drive
import org.notests.sharedsequence.empty
import org.notests.sharedsequence.map

/**
 * Created by jurajbegovac on 14/06/2018.
 */

interface CryptoDetailsView : RxFeedbackView<State, Event> {
  val swipeRefreshLayout: SwipeRefreshLayout
  fun showError(@StringRes errorResId: Int)
  fun showLoading(loading: Boolean)
  fun showDetails(details: List<CryptoDetailItem>)
  fun setTitle(title: String)

  override fun invoke(state: Driver<State>): Bindings<Event> {
    // state
    val titleDisposable = state.title.drive { setTitle(it) }
    val detailsDisposable = state.details.drive { showDetails(it) }
    val loadingDisposable = state.load.drive { showLoading(it) }
    val errorDisposable = state.errorResId.drive { showError(it) }

    // events
    val swipeToRefreshEvent = swipeRefreshLayout.reloadEvent

    val subscriptions = listOf(titleDisposable, detailsDisposable, loadingDisposable, errorDisposable)
    val events = listOf(swipeToRefreshEvent)
    return Bindings.safe(subscriptions, events)
  }
}

/**
 * State -> View
 */
val Driver<State>.title
  get() = crypto.map { it.name }.distinctUntilChanged()

val Driver<State>.details
  get() = crypto.map { it.detailItems }

val Driver<State>.errorResId
  get() = error.map { it.errorStringResId }

/**
 * View -> Event
 */
val SwipeRefreshLayout.reloadEvent: Signal<Event>
  get() = RxSwipeRefreshLayout.refreshes(this).map<Event> { Event.ReloadData }.asSignal { Signal.empty() }

/**
 * Mappers
 */
private val StateError.errorStringResId
  get() =
    when (this) {
      StateError.NoInternet -> R.string.error_no_internet
      StateError.Unknown    -> R.string.error_unknown
    }

val Crypto.detailItems: List<CryptoDetailItem>
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

data class CryptoDetailItem(@StringRes val nameResId: Int, val value: String) {
  companion object {
    val DIFF = object : DiffUtil.ItemCallback<CryptoDetailItem>() {
      override fun areItemsTheSame(oldItem: CryptoDetailItem, newItem: CryptoDetailItem) = oldItem.nameResId ==
        newItem.nameResId

      override fun areContentsTheSame(oldItem: CryptoDetailItem, newItem: CryptoDetailItem): Boolean =
        oldItem == newItem
    }
  }
}
