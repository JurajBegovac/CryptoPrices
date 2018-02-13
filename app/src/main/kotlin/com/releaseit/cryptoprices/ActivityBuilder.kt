package com.releaseit.cryptoprices

import com.releaseit.cryptoprices.main.FragmentBuilder
import com.releaseit.cryptoprices.main.MainActivity
import com.releaseit.cryptoprices.main.MainActivityModule
import com.releaseit.rxrepository.dagger2.qualifiers.PerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by jurajbegovac on 27/01/2018.
 */
@Module
abstract class ActivityBuilder {

  @PerActivity
  @ContributesAndroidInjector(modules = [MainActivityModule::class, FragmentBuilder::class])
  abstract fun bindMainActivity(): MainActivity

}
