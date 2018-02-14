package com.releaseit.cryptoprices.details

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import java.net.UnknownHostException

/**
 * Created by jurajbegovac on 13/02/2018.
 */

data class State(val crypto: Crypto?, val error: StateError?, val showLoading: Boolean)

sealed class StateError {
  object NoInternet : StateError()
  object Unknown : StateError()
}

class CryptoDetailsViewModel(private val id: String,
                             private val repository: CryptoRepository,
                             private val prefs: Prefs,
                             private val schedulerProvider: SchedulerProvider) : ViewModel() {

  private val compositeDisposable = CompositeDisposable()

  val state = MutableLiveData<State>()

  init {
    compositeDisposable.addAll(loadCryptoDisposable(), currencyDisposable())
  }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }

  private fun loadCryptoDisposable() =
    repository.getCrypto(id, prefs.currency)
      .subscribeOn(schedulerProvider.io())
      .observeOn(schedulerProvider.main())
      .subscribe({ success, error ->
                   if (success != null) {
                     state.value = State(success, null, false)
                   } else if (error != null) {
                     val currentState = state.value
                     val stateError =
                       if (error is UnknownHostException) StateError.NoInternet else StateError.Unknown
                     state.value = currentState?.copy(error = stateError, showLoading = false) ?:
                       State(null, stateError, false)
                   }
                 })

  private fun currencyDisposable() = prefs.currencyObservable.subscribe { reloadData() }

  /**
   * Command that view can use for reloading data
   */
  fun reloadData() {
    // show loading
    state.value = state.value?.copy(showLoading = true) ?: State(null, null, true)
    // make new query
    compositeDisposable.add(loadCryptoDisposable())
  }

}

class CryptoDetailsViewModelFactory(private val id: String,
                                    private val repository: CryptoRepository,
                                    private val prefs: Prefs,
                                    private val schedulerProvider: SchedulerProvider) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CryptoDetailsViewModel::class.java)) {
      return CryptoDetailsViewModel(id, repository, prefs, schedulerProvider) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
