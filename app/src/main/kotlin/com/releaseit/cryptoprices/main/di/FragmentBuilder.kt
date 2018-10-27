package com.releaseit.cryptoprices.main.di

import com.releaseit.cryptoprices.details.CryptoDetailsFragment
import com.releaseit.cryptoprices.details.CryptoDetailsModule
import com.releaseit.cryptoprices.list.CryptoListFragment
import com.releaseit.cryptoprices.list.CryptoListFragmentModule
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by jurajbegovac on 14/02/2018.
 */
@Module
abstract class FragmentBuilder {

  @PerFragment
  @ContributesAndroidInjector(modules = [CryptoListFragmentModule::class])
  abstract fun cryptoListFragment(): CryptoListFragment

  @PerFragment
  @ContributesAndroidInjector(modules = [CryptoDetailsModule::class])
  abstract fun cryptoDetailsFragment(): CryptoDetailsFragment

}
