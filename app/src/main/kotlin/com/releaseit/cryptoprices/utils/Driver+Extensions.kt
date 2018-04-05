package com.releaseit.cryptoprices.utils

import org.notests.sharedsequence.Driver

/**
 * Created by jurajbegovac on 05/04/2018.
 */

fun <Element, Result> Driver<Element>.cast(clazz: Class<Result>): Driver<Result> =
        Driver<Result>(this.asObservable().cast(clazz))
