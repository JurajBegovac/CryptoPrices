package com.releaseit.cryptoprices

import android.app.Activity
import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

/**
 * Created by jurajbegovac on 12/02/2018.
 */

class App : Application(), HasActivityInjector {

  @Inject
  lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

  private lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()
    appComponent =
      DaggerAppComponent.builder()
        .application(this)
        .build()
    appComponent.inject(this)
  }

  override fun activityInjector(): AndroidInjector<Activity> {
    return dispatchingActivityInjector
  }
}
