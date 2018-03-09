package com.releaseit.cryptoprices.utils

import android.content.Context
import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.releaseit.cryptoprices.repository.Currency
import com.releaseit.cryptoprices.utils.dagger2.qualifiers.ApplicationContext
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by jurajbegovac on 13/02/2018.
 */
const val PACKAGE = "com.releseit.cryptoprices"
const val PREF_NAME = "${PACKAGE}.prefs"
const val KEY = "${PREF_NAME}.key"
const val KEY_CURRENCY = "${KEY}.currency"

fun SharedPreferences.currency() = Currency.valueOf(getString(KEY_CURRENCY, Currency.USD.name))
fun SharedPreferences.saveCurrency(currency: Currency) = edit().putString(KEY_CURRENCY, currency.name).commit()

// rx
fun SharedPreferences.rx() = RxSharedPreferences.create(this)

fun SharedPreferences.currencyObservable() = rx().getString(KEY_CURRENCY, Currency.USD.name)
        .asObservable().map { Currency.valueOf(it) }

interface Prefs {
    var currency: Currency
    val currencyObservable: Observable<Currency>
}

@Singleton
class DefaultPrefs @Inject constructor(@ApplicationContext context: Context) : Prefs {

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override var currency: Currency
        get() = sharedPreferences.currency()
        set(value) {
            sharedPreferences.saveCurrency(value)
        }
    override val currencyObservable: Observable<Currency>
        get() = sharedPreferences.currencyObservable()
}
