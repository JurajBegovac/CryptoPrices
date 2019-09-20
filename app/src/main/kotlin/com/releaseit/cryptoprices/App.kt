package com.releaseit.cryptoprices

import android.app.Application
import com.releaseit.cryptoprices.details.CryptoDetailsViewModel
import com.releaseit.cryptoprices.details.State
import com.releaseit.cryptoprices.details.currencyDetailsFeedback
import com.releaseit.cryptoprices.details.loadingFeedback
import com.releaseit.cryptoprices.details.reduce
import com.releaseit.cryptoprices.list.CryptoListViewModel
import com.releaseit.cryptoprices.list.currencyListFeedback
import com.releaseit.cryptoprices.list.reduce
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.WebCryptoRepository
import com.releaseit.cryptoprices.utils.DefaultPrefs
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.web.CryptoWebService
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by jurajbegovac on 12/02/2018.
 */

class App : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger()
      androidContext(this@App)
      modules(appModule)
    }
  }

  private val appModule = module {

    single<Prefs> { DefaultPrefs(androidContext()) }

    single<OkHttpClient> {
      OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()
    }

    single<Retrofit> {
      Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .client(get<OkHttpClient>())
        .build()
    }

    single<CryptoWebService> { get<Retrofit>().create<CryptoWebService>(CryptoWebService::class.java) }

    single<CryptoRepository> { WebCryptoRepository(get()) }

    viewModel(qualifier = named("CryptoDetailsViewModel")) { (id: String) ->
      CryptoDetailsViewModel(State.initial,
                             { s, e -> State.reduce(s, e) },
                             listOf(get<Prefs>().currencyDetailsFeedback,
                                    loadingFeedback(get(), get(), id)))
    }

    viewModel(qualifier = named("CryptoListViewModel")) {
      CryptoListViewModel(com.releaseit.cryptoprices.list.State.initial(),
                          { s, e -> com.releaseit.cryptoprices.list.State.reduce(s, e) },
                          listOf(get<Prefs>().currencyListFeedback,
                                 com.releaseit.cryptoprices.list.loadingFeedback(get(), get())))
    }
  }
}
