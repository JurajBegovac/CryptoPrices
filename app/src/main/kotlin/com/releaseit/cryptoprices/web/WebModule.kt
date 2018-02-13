package com.releaseit.cryptoprices.web

import com.releaseit.cryptoprices.BuildConfig
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by jurajbegovac on 12/02/2018.
 */

@Module
class WebModule {

  @Singleton
  @Provides
  fun okHttpClient(): OkHttpClient {
    val builder = OkHttpClient.Builder()
      .readTimeout(1, TimeUnit.MINUTES)
      .writeTimeout(1, TimeUnit.MINUTES)
    return builder.build()
  }

  @Provides
  @Singleton
  fun retrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(BuildConfig.API_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
      .client(okHttpClient)
      .build()
  }

  @Provides
  @Singleton
  fun cryptoWebService(retrofit: Retrofit): CryptoWebService {
    return retrofit.create<CryptoWebService>(CryptoWebService::class.java)
  }
}
