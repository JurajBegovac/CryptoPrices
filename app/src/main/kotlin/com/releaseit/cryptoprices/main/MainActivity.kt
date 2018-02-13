package com.releaseit.cryptoprices.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.list.CryptoListFragment
import com.releaseit.cryptoprices.settings.SettingsActivity
import dagger.android.support.DaggerAppCompatActivity

class MainActivity : DaggerAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null)
      supportFragmentManager.beginTransaction()
        .replace(R.id.mainActivityContainer, CryptoListFragment.newInstance())
        .commit()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.menu_action_settings) {
      showSettings()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun showSettings() {
    startActivity(SettingsActivity.startIntent(this))
  }
}
