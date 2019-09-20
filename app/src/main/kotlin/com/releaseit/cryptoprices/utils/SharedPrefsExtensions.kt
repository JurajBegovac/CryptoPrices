package com.releaseit.cryptoprices.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.releaseit.cryptoprices.repository.Currency
import io.reactivex.Observable

/**
 * Created by jurajbegovac on 13/02/2018.
 */
const val PACKAGE = "com.releseit.cryptoprices"
const val PREF_NAME = "$PACKAGE.prefs"
const val KEY = "$PREF_NAME.key"
const val KEY_CURRENCY = "$KEY.currency"

fun SharedPreferences.currency() = Currency.valueOf(getString(KEY_CURRENCY, Currency.USD.name)!!)
fun SharedPreferences.saveCurrency(currency: Currency) = edit(true) {
  putString(KEY_CURRENCY, currency.name)
}

// rx
fun SharedPreferences.rx() = RxSharedPreferences.create(this)

fun RxSharedPreferences.currencyObservable(): Observable<Currency> =
  getString(KEY_CURRENCY, Currency.USD.name).asObservable().map { Currency.valueOf(it) }

interface Prefs {
  var currency: Currency
  val currencyObservable: Observable<Currency>
}

class DefaultPrefs constructor(context: Context) : Prefs {

  private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
  private val rxSharedPreferences = sharedPreferences.rx()

  override var currency: Currency
    get() = sharedPreferences.currency()
    set(value) {
      sharedPreferences.saveCurrency(value)
    }

  override val currencyObservable: Observable<Currency>
    get() = rxSharedPreferences.currencyObservable()
}
