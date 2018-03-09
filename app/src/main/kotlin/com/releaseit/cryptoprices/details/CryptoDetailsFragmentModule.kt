/*
 * Copyright © 2014-2018, TWINT AG.
 * All rights reserved.
 */

package com.releaseit.cryptoprices.details

import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.SchedulerProvider
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class CryptoDetailsFragmentModule {

    @Provides
    @PerFragment
    @Named(CryptoDetailsFragment.KEY_ID)
    fun id(cryptoDetailsFragment: CryptoDetailsFragment): String =
            cryptoDetailsFragment.arguments.getString(CryptoDetailsFragment.KEY_ID)!!

    @Provides
    @PerFragment
    fun cryptoDetailsViewModelFactory(@Named(CryptoDetailsFragment.KEY_ID) id: String, repository: CryptoRepository,
                                      prefs: Prefs, schedulerProvider: SchedulerProvider) =
            CryptoDetailsViewModelFactory(id, repository, prefs, schedulerProvider)
}
