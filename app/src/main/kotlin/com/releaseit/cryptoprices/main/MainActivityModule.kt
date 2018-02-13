package com.releaseit.cryptoprices.main

import android.content.SharedPreferences
import com.releaseit.cryptoprices.list.CryptoListFragment
import com.releaseit.cryptoprices.list.CryptoListViewModelFactory
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.rxrepository.dagger2.qualifiers.PerFragment
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

/**
 * Created by jurajbegovac on 13/02/2018.
 */
@Module
class MainActivityModule {

  @Provides
  fun cryptoListViewModelFactory(cryptoRepository: CryptoRepository, sharedPreferences: SharedPreferences) =
    CryptoListViewModelFactory(cryptoRepository, sharedPreferences)
}

@Module
abstract class FragmentBuilder {

  @PerFragment
  @ContributesAndroidInjector
  abstract fun cryptoListFragment(): CryptoListFragment

}
