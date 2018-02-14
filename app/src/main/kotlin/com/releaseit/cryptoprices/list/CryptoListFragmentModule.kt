package com.releaseit.cryptoprices.list

import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.SchedulerProvider
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import dagger.Module
import dagger.Provides

/**
 * Created by jurajbegovac on 14/02/2018.
 */
@Module
class CryptoListFragmentModule {

  @Provides
  @PerFragment
  fun cryptoListViewModelFactory(cryptoRepository: CryptoRepository,
                                 prefs: Prefs,
                                 schedulerProvider: SchedulerProvider) =
    CryptoListViewModelFactory(cryptoRepository, prefs, schedulerProvider)
}
