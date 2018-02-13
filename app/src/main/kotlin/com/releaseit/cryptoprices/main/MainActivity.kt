package com.releaseit.cryptoprices.main

import android.os.Bundle
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.list.CryptoListFragment
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
}
