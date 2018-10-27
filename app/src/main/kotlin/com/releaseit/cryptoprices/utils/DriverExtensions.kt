package com.releaseit.cryptoprices.utils

import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import org.notests.sharedsequence.Driver

/**
 * Created by jurajbegovac on 05/04/2018.
 */

fun <Element, Result> Driver<Element>.cast(clazz: Class<Result>): Driver<Result> =
  Driver<Result>(this.asObservable().cast(clazz))

fun <Element> Driver<Element>.toLiveData(backpressureStrategy: BackpressureStrategy = BackpressureStrategy.LATEST) =
  LiveDataReactiveStreams.fromPublisher(this.asObservable().toFlowable(backpressureStrategy))
