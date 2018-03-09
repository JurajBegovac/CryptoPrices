package com.releaseit.cryptoprices.utils

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by jurajbegovac on 14/02/2018.
 */

interface SchedulerProvider {
    fun io(): Scheduler
    fun main(): Scheduler
}

@Singleton
class DefaultSchedulerProvider @Inject constructor() : SchedulerProvider {
    override fun io(): Scheduler = Schedulers.io()
    override fun main(): Scheduler = AndroidSchedulers.mainThread()
}
