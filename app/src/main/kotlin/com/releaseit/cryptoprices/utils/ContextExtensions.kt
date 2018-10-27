package com.releaseit.cryptoprices.utils

import android.content.Context
import androidx.annotation.StringRes
import android.widget.Toast

/**
 * Created by jurajbegovac on 13/02/2018.
 */

fun Context.showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.showToast(@StringRes msgId: Int) = Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()
