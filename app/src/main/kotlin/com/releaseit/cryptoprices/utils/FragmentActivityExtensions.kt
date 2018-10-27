/*
 * Copyright © 2014-2018, TWINT AG.
 * All rights reserved.
 */

package com.releaseit.cryptoprices.utils

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.transaction

fun FragmentActivity.showFragment(fragment: Fragment, @IdRes container: Int,
                                  withBackStack: Boolean = false) =
  supportFragmentManager.transaction {
    if (withBackStack) addToBackStack(fragment.javaClass.canonicalName)
    replace(container, fragment)
  }
