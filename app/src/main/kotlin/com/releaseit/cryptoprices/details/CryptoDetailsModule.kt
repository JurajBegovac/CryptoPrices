package com.releaseit.cryptoprices.details

import com.releaseit.cryptoprices.navigation.Navigator
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.dagger2.scopes.PerFragment
import dagger.Module
import dagger.Provides
import org.notests.rxfeedback.system
import org.notests.sharedsequence.Driver
import javax.inject.Named

/**
 * Created by jurajbegovac on 14/06/2018.
 */

/**
 * DI - dagger
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
  fun view(fragment: CryptoDetailsFragment): CryptoDetailsView = fragment

  @Provides
  @PerFragment
  fun rxFeedback(@Named(CryptoDetailsFragment.KEY_ID) id: String,
                 repository: CryptoRepository,
                 prefs: Prefs,
                 view: CryptoDetailsView,
                 navigator: Navigator): Driver<State> =
    Driver.system(State.initial,
                  { s, e -> State.reduce(s, e) },
                  listOf(repository.feedback(id), prefs.feedback, view.feedback(navigator)))
}
