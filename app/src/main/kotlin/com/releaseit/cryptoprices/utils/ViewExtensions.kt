package com.releaseit.cryptoprices.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * Created by jurajbegovac on 13/02/2018.
 */
fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false): View =
  LayoutInflater.from(context).inflate(resource, this, attachToRoot)
