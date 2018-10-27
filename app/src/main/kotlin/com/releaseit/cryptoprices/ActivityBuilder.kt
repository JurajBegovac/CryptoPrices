package com.releaseit.cryptoprices

import com.releaseit.cryptoprices.main.MainActivity
import com.releaseit.cryptoprices.main.di.FragmentBuilder
import com.releaseit.cryptoprices.settings.SettingsActivity
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by jurajbegovac on 27/01/2018.
 */
@Module
abstract class ActivityBuilder {

  @PerActivity
  @ContributesAndroidInjector(modules = [FragmentBuilder::class])
  abstract fun bindMainActivity(): MainActivity

  @PerActivity
  @ContributesAndroidInjector
  abstract fun bindSettingsActivity(): SettingsActivity

}
