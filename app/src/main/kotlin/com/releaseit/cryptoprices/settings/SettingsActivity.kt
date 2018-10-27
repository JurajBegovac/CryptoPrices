package com.releaseit.cryptoprices.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.repository.Currency
import com.releaseit.cryptoprices.utils.Prefs
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*
import javax.inject.Inject

class SettingsActivity : DaggerAppCompatActivity() {

  companion object {
    fun startIntent(context: Context) = Intent(context, SettingsActivity::class.java)
  }

  @Inject
  lateinit var prefs: Prefs

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)

    supportActionBar?.apply {
      title = getString(R.string.settings_title)
      setDisplayHomeAsUpEnabled(true)
      setDisplayShowHomeEnabled(true)
    }

    val currentCurrency = prefs.currency

    Currency.values()
      .forEach { currency ->
        val radioButton = RadioButton(this)
        radioButton.text = currency.name
        settingsActivityRadioGroup.addView(radioButton)
        if (currency == currentCurrency) radioButton.isChecked = true
        radioButton.setOnCheckedChangeListener { _, isChecked ->
          if (isChecked) {
            currency.selected()
          }
        }
      }
  }

  override fun onSupportNavigateUp() = true.apply {
    onBackPressed()
  }

  private fun Currency.selected() {
    prefs.currency = this
  }
}
