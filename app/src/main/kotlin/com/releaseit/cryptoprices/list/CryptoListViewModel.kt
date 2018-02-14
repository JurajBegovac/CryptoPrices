package com.releaseit.cryptoprices.list

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
 * Created by jurajbegovac on 12/02/2018.
 */

data class State(val items: List<Crypto>, val error: StateError?, val showLoading: Boolean) {
  companion object {
    fun initial() = State(emptyList(), null, true)
  }
}

sealed class StateError {
  object NoInternet : StateError()
  object Unknown : StateError()
}

class CryptoListViewModel(private val repository: CryptoRepository,
                          private val prefs: Prefs,
                          private val schedulerProvider: SchedulerProvider) : ViewModel() {

  private val compositeDisposable = CompositeDisposable()

  val state = MutableLiveData<State>()

  init {
    state.value = State.initial()
    compositeDisposable.addAll(loadCryptosDisposable(), currencyDisposable())
  }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }

  private fun loadCryptosDisposable() =
    repository.getCryptos(prefs.currency, "100")
      .subscribeOn(schedulerProvider.io())
      .observeOn(schedulerProvider.main())
      .subscribe({ success, error ->
                   if (success != null) {
                     state.value = State(success, null, false)
                   } else if (error != null) {
                     val currentState = state.value ?: State.initial()
                     val stateError =
                       if (error is UnknownHostException) StateError.NoInternet else StateError.Unknown
                     state.value = currentState.copy(error = stateError, showLoading = false)
                   }
                 })

  private fun currencyDisposable() = prefs.currencyObservable.subscribe { reloadData() }

  /**
   * Command that view can use for reloading data
   */
  fun reloadData() {
    // show loading
    state.value = state.value?.copy(showLoading = true) ?: State.initial().copy(showLoading = true)
    // make new query
    compositeDisposable.add(loadCryptosDisposable())
  }

  fun itemId(position: Int) = state.value!!.items[position].id

}

class CryptoListViewModelFactory(private val repository: CryptoRepository,
                                 private val prefs: Prefs,
                                 private val schedulerProvider: SchedulerProvider) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CryptoListViewModel::class.java)) {
      return CryptoListViewModel(repository, prefs, schedulerProvider) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
