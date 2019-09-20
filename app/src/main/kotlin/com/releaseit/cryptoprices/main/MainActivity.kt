package com.releaseit.cryptoprices.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.details.CryptoDetailsFragment
import com.releaseit.cryptoprices.list.CryptoListFragment
import com.releaseit.cryptoprices.navigation.Navigator
import com.releaseit.cryptoprices.navigation.Screen
import com.releaseit.cryptoprices.settings.SettingsActivity
import com.releaseit.cryptoprices.utils.showFragment

class MainActivity : AppCompatActivity(), Navigator {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null)
      to(Screen.CryptoList)
  }

  override fun to(screen: Screen) =
    when (screen) {
      Screen.CryptoList       -> showFragment(CryptoListFragment.newInstance(),
                                              R.id.mainActivityContainer)
      is Screen.CryptoDetails -> showFragment(CryptoDetailsFragment.newInstance(screen.id),
                                              R.id.mainActivityContainer, true)
      Screen.Settings         -> startActivity(SettingsActivity.startIntent(this))
    }

  override fun back() = onBackPressed()
}
