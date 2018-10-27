package com.releaseit.cryptoprices

import android.content.Context
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.WebCryptoRepository
import com.releaseit.cryptoprices.utils.DefaultPrefs
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.dagger2.qualifiers.ApplicationContext
import com.releaseit.cryptoprices.web.WebModule
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Created by jurajbegovac on 14/02/2018.
 */
// DI
@Module
abstract class AppModule {

  @Binds
  @ApplicationContext
  abstract fun context(app: App): Context

  @Binds
  abstract fun cryptoRepository(cryptoRepository: WebCryptoRepository): CryptoRepository

  @Binds
  abstract fun prefs(prefs: DefaultPrefs): Prefs

}

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, ActivityBuilder::class, AppModule::class, WebModule::class])
interface AppComponent {

  fun inject(target: App)

  @Component.Builder
  interface Builder {

    @BindsInstance
    fun application(app: App): Builder

    fun build(): AppComponent
  }
}
