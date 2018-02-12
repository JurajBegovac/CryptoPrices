package com.releaseit.cryptoprices

import android.app.Activity
import android.app.Application
import android.content.Context
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.WebCryptoRepository
import com.releaseit.cryptoprices.web.CryptoWebService
import com.releaseit.cryptoprices.web.WebModule
import com.releaseit.rxrepository.dagger2.qualifiers.ApplicationContext
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Inject
import javax.inject.Singleton

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

// DI
@Module
class AppModule {

  @Provides
  @Singleton
  @ApplicationContext
  fun context(app: App): Context {
    return app
  }

  @Provides
  @Singleton
  fun cryptoRepository(webService: CryptoWebService): CryptoRepository = WebCryptoRepository(webService)
}

@Singleton
@Component(modules = [
  AndroidSupportInjectionModule::class,
  ActivityBuilder::class,
  AppModule::class,
  WebModule::class
])
interface AppComponent {

  fun inject(target: App)

  @Component.Builder
  interface Builder {

    @BindsInstance
    fun application(app: App): Builder

    fun build(): AppComponent
  }
}
