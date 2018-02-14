package com.releaseit.cryptoprices.list

import android.content.SharedPreferences
import com.releaseit.cryptoprices.repository.CryptoRepository
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
  fun cryptoListViewModelFactory(cryptoRepository: CryptoRepository, sharedPreferences: SharedPreferences) =
    CryptoListViewModelFactory(cryptoRepository, sharedPreferences)
}
