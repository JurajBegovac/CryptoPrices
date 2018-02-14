package com.releaseit.cryptoprices.main

import android.os.Bundle
import android.support.v4.app.Fragment
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.details.CryptoDetailsFragment
import com.releaseit.cryptoprices.list.CryptoListFragment
import com.releaseit.cryptoprices.navigation.Navigator
import com.releaseit.cryptoprices.navigation.Screen
import com.releaseit.cryptoprices.settings.SettingsActivity
import dagger.android.support.DaggerAppCompatActivity

class MainActivity : DaggerAppCompatActivity(), Navigator {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null)
      navigateTo(Screen.CryptoList)
  }

  override fun navigateTo(screen: Screen) =
    when (screen) {
      Screen.CryptoList       -> showFragment(CryptoListFragment.newInstance())
      is Screen.CryptoDetails -> showFragmentWithBackStack(CryptoDetailsFragment.newInstance(screen.id))
      Screen.Settings         -> startActivity(SettingsActivity.startIntent(this))
    }

  override fun navigateBack() {
    onBackPressed()
  }

  private fun showFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
      .replace(R.id.mainActivityContainer, fragment)
      .commit()
  }

  private fun showFragmentWithBackStack(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
      .addToBackStack(null)
      .replace(R.id.mainActivityContainer, fragment)
      .commit()
  }
}
