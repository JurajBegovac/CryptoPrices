package com.releaseit.cryptoprices.utils

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by jurajbegovac on 13/02/2018.
 */
fun ViewGroup.inflate(@LayoutRes resource: Int): View = LayoutInflater.from(context).inflate(resource, this, false)
