package com.releaseit.cryptoprices

import android.content.Context
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.Currency
import com.releaseit.cryptoprices.repository.WebCryptoRepository
import com.releaseit.cryptoprices.utils.PREF_NAME
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.SchedulerProvider
import com.releaseit.cryptoprices.utils.currency
import com.releaseit.cryptoprices.utils.currencyObservable
import com.releaseit.cryptoprices.utils.dagger2.qualifiers.ApplicationContext
import com.releaseit.cryptoprices.utils.saveCurrency
import com.releaseit.cryptoprices.web.CryptoWebService
import com.releaseit.cryptoprices.web.WebModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.support.AndroidSupportInjectionModule
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton

/**
 * Created by jurajbegovac on 14/02/2018.
 */
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

  @Provides
  fun prefs(@ApplicationContext context: Context): Prefs {
    val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return object : Prefs {
      override var currency: Currency
        get() = sharedPreferences.currency()
        set(value) {
          sharedPreferences.saveCurrency(value)
        }
      override val currencyObservable: Observable<Currency>
        get() = sharedPreferences.currencyObservable()
    }
  }

  @Provides
  fun schedulerProvider() = object : SchedulerProvider {
    override fun io() = Schedulers.io()

    override fun main() = AndroidSchedulers.mainThread()
  }

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
