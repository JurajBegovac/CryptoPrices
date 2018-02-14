package com.releaseit.cryptoprices.main.di

import com.releaseit.cryptoprices.main.MainActivity
import com.releaseit.cryptoprices.navigation.Navigator
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerActivity
import dagger.Module
import dagger.Provides

/**
 * Created by jurajbegovac on 13/02/2018.
 */
@Module
class MainActivityModule {

  @Provides
  @PerActivity
  fun navigator(activity: MainActivity): Navigator = activity
}
