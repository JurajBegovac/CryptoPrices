package com.releaseit.cryptoprices.list

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.SharedPreferences
import com.releaseit.cryptoprices.currency
import com.releaseit.cryptoprices.currencyObservable
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException

/**
 * Created by jurajbegovac on 12/02/2018.
 */

data class State(val items: List<Crypto>, val error: Error?, val showLoading: Boolean) {
  companion object {
    fun initial() = State(emptyList(), null, true)
  }
}

sealed class StateError {
  object NoInternet : Error()
  object Unknown : Error()
}

class CryptoListViewModel(private val repository: CryptoRepository,
                          private val sharedPreferences: SharedPreferences) : ViewModel() {

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
    repository.getCryptos(sharedPreferences.currency(), "100")
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
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

  private fun currencyDisposable() = sharedPreferences.currencyObservable().subscribe { reloadData() }

  /**
   * Command that view can use for reloading data
   */
  fun reloadData() {
    // show loading
    state.value = state.value?.copy(showLoading = true) ?: State.initial().copy(showLoading = true)
    // make new query
    compositeDisposable.add(loadCryptosDisposable())
  }
}

class CryptoListViewModelFactory(private val repository: CryptoRepository,
                                 private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CryptoListViewModel::class.java)) {
      return CryptoListViewModel(repository, sharedPreferences) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
