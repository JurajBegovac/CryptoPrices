package com.releaseit.cryptoprices.details

import android.support.annotation.StringRes
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.navigation.Navigator
import com.releaseit.cryptoprices.navigation.Screen
import com.releaseit.cryptoprices.repository.Crypto
import org.notests.rxfeedback.Bindings
import org.notests.rxfeedback.Optional
import org.notests.rxfeedback.SignalFeedback
import org.notests.rxfeedback.bindSafe
import org.notests.sharedsequence.Driver
import org.notests.sharedsequence.Signal
import org.notests.sharedsequence.asSignal
import org.notests.sharedsequence.distinctUntilChanged
import org.notests.sharedsequence.drive
import org.notests.sharedsequence.empty
import org.notests.sharedsequence.filter
import org.notests.sharedsequence.map

/**
 * Created by jurajbegovac on 14/06/2018.
 */

interface CryptoDetailsView {
  fun showTitle(title: String)
  fun showCryptoDetails(details: List<CryptoDetailItem>)
  fun showLoading(loading: Boolean)
  fun showError(@StringRes errorMsgId: Int)

  val toolbar: Toolbar
  val swipeRefreshLayout: SwipeRefreshLayout
}

// UI feedback - this is "presenter"
fun CryptoDetailsView.feedback(navigator: Navigator): SignalFeedback<State, Event> = bindSafe {
  val titleDisposable = it.cryptoItemDriver
    .map { it.name }
    .distinctUntilChanged()
    .drive { this.showTitle(it) }

  val detailsDisposable = it.cryptoItemDriver
    .map { it.detailItems }
    .distinctUntilChanged()
    .drive { this.showCryptoDetails(it) }

  val loadingDisposable = it.loadingDriver
    .drive { this.showLoading(it) }

  val errorDisposable = it.errorDriver
    .map { toErrorStringRes(it) }
    .drive { this.showError(it) }

  val refreshEvent: Signal<Event> = RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
    .asSignal(onError = org.notests.sharedsequence.Signal.empty())
    .map { Event.ReloadData }

  val toolbarItemClickDisposable = RxToolbar.itemClicks(toolbar)
    .subscribe {
      if (it.itemId == R.id.menu_action_settings) {
        navigator.navigateTo(Screen.Settings)
      }
    }

  val toolbarNavigationDisposable = RxToolbar.navigationClicks(toolbar)
    .subscribe {
      navigator.navigateBack()
    }

  Bindings.safe(listOf(titleDisposable, detailsDisposable, loadingDisposable, errorDisposable,
                       toolbarItemClickDisposable, toolbarNavigationDisposable),
                listOf(refreshEvent))
}


/**
 * Extensions and helpers
 */
val Driver<State>.cryptoItemDriver
  get() =
    this
      .map { it.crypto }
      .filter { it is Optional.Some }
      .map { (it as Optional.Some).data }
      .distinctUntilChanged()

val Driver<State>.loadingDriver
  get() =
    this
      .map { it.loading }
      .distinctUntilChanged()

val Driver<State>.errorDriver
  get() = this
    .map { it.error }
    .filter { it !is Optional.None }
    .map { (it as Optional.Some).data }

@StringRes
private fun toErrorStringRes(error: StateError) =
  when (error) {
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
