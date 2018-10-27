package com.releaseit.cryptoprices.details

import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Created by jurajbegovac on 14/06/2018.
 */

@Module
class CryptoDetailsModule {

  @Provides
  @PerFragment
  @Named(CryptoDetailsFragment.KEY_ID)
  fun id(cryptoDetailsFragment: CryptoDetailsFragment): String =
    cryptoDetailsFragment.arguments!!.getString(CryptoDetailsFragment.KEY_ID)!!

  @Provides
  @PerFragment
  fun cryptoListViewModelFactory(@Named(CryptoDetailsFragment.KEY_ID) id: String,
                                 cryptoRepository: CryptoRepository,
                                 prefs: Prefs): CryptoDetailsViewModelFactory =
    object : CryptoDetailsViewModelFactory {
      override fun invoke() =
        CryptoDetailsViewModel(State.initial,
                               { s, e -> State.reduce(s, e) },
                               listOf(prefs.currencyFeedback, loadingFeedback(cryptoRepository, prefs, id)))
    }
}
