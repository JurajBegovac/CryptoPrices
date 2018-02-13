package com.releaseit.cryptoprices.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.currency
import com.releaseit.cryptoprices.repository.Currency
import com.releaseit.cryptoprices.saveCurrency
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*
import javax.inject.Inject

class SettingsActivity : DaggerAppCompatActivity() {

  companion object {
    fun startIntent(context: Context) = Intent(context, SettingsActivity::class.java)
  }

  @Inject
  lateinit var sharedPrefs: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)

    supportActionBar?.apply {
      title = getString(R.string.settings_title)
      setDisplayHomeAsUpEnabled(true)
      setDisplayShowHomeEnabled(true)
    }

    val currentCurrency = sharedPrefs.currency()

    Currency.values()
      .forEach {
        val radioButton = RadioButton(this)
        radioButton.text = it.name
        settingsActivityRadioGroup.addView(radioButton)
        if (it == currentCurrency) radioButton.isChecked = true
        radioButton.setOnCheckedChangeListener { _, isChecked ->
          if (isChecked) {
            currencySelected(it)
          }
        }
      }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  private fun currencySelected(currency: Currency) {
    sharedPrefs.saveCurrency(currency)
  }
}
