package com.releaseit.cryptoprices.navigation

import androidx.fragment.app.Fragment

/** Created by jurajbegovac on 14/02/2018. */

/**
 * Navigator
 */
interface Navigator {
  companion object

  fun to(screen: Screen)
  fun back()
}

/**
 * Screens
 */
sealed class Screen {
  object CryptoList : Screen()
  data class CryptoDetails(val id: String) : Screen()
  object Settings : Screen()
}

/**
 * Helpers
 */
val Fragment.navigation: Navigator
  get() = object : Navigator {
    private val navigator = activity as? Navigator

    override fun to(screen: Screen) = navigator?.to(screen) ?: Unit
    override fun back() = navigator?.back() ?: Unit
  }
