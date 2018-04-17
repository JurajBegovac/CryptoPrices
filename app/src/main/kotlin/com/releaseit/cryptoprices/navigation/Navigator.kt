package com.releaseit.cryptoprices.navigation

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/** Created by jurajbegovac on 14/02/2018. */

/**
 * Navigator
 */
interface Navigator {
  companion object

  fun navigateTo(screen: Screen)
  fun navigateBack()
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
 * Extensions (helpers)
 */
fun Navigator.Companion.showFragmentWithBackStack(activity: FragmentActivity,
                                                  fragment: Fragment, @IdRes container: Int) {
  activity.supportFragmentManager.beginTransaction()
    .addToBackStack(null)
    .replace(container, fragment)
    .commit()
}

fun Navigator.Companion.showFragment(activity: FragmentActivity, fragment: Fragment, @IdRes container: Int) {
  activity.supportFragmentManager.beginTransaction()
    .replace(container, fragment)
    .commit()
}
