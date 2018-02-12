package com.releaseit.cryptoprices.list

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.Currency
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.UnknownHostException

/**
 * Created by jurajbegovac on 12/02/2018.
 */

data class State(val items: List<Crypto>, val error: Error?)

sealed class StateError {
  object NoInternet : Error()
  object Unknown : Error()
}

class CryptoListViewModel(private val repository: CryptoRepository) : ViewModel() {

  private val compositeDisposable = CompositeDisposable()

  val state = MutableLiveData<State>()

  init {
    compositeDisposable.add(loadCryptosDisposable())
  }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }

  fun reloadData() {
    compositeDisposable.clear()
    compositeDisposable.add(loadCryptosDisposable())
  }

  private fun loadCryptosDisposable() =
    repository.getCryptos(Currency.USD, "100")
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ success, error ->
                   if (success != null) {
                     state.value = State(success, null)
                   } else if (error != null) {
                     val currentItems = state.value?.items ?: emptyList()
                     val stateError =
                       if (error is UnknownHostException) StateError.NoInternet else StateError.Unknown
                     state.value = State(currentItems, stateError)
                   }
                 })
}

class CryptoListViewModelFactory(private val repository: CryptoRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CryptoListViewModel::class.java)) {
      return CryptoListViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
