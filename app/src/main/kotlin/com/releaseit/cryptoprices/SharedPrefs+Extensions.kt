package com.releaseit.cryptoprices

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.releaseit.cryptoprices.repository.Currency

/**
 * Created by jurajbegovac on 13/02/2018.
 */
const val PACKAGE = "com.releseit.cryptoprices"
const val PREF_NAME = "$PACKAGE.prefs"
const val KEY = "$PREF_NAME.key"
const val KEY_CURRENCY = "$KEY.currency"

fun SharedPreferences.currency() = Currency.valueOf(getString(KEY_CURRENCY, Currency.USD.name))
fun SharedPreferences.saveCurrency(currency: Currency) = edit().putString(KEY_CURRENCY, currency.name).commit()

// rx
fun SharedPreferences.rx() = RxSharedPreferences.create(this)

fun SharedPreferences.currencyObservable() = rx().getString(KEY_CURRENCY, Currency.USD.name)
  .asObservable().map { Currency.valueOf(it) }
