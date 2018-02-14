package com.releaseit.cryptoprices.utils

import io.reactivex.Scheduler

/**
 * Created by jurajbegovac on 14/02/2018.
 */

interface SchedulerProvider {
  fun io(): Scheduler
  fun main(): Scheduler
}
