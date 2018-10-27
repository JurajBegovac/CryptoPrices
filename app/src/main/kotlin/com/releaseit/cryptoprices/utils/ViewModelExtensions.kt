package com.releaseit.cryptoprices.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

inline fun <reified T : ViewModel> ViewModelProvider.get(): T = get(T::class.java)

inline fun <reified T : ViewModel> FragmentActivity.viewModel(viewModelFactory: ViewModelProvider.Factory? = null) =
  viewModel<FragmentActivity, T>(this, viewModelFactory)

inline fun <reified T : ViewModel> Fragment.viewModel(viewModelFactory: ViewModelProvider.Factory? = null) =
  viewModel<Fragment, T>(this, viewModelFactory)

inline fun <reified T : ViewModel> Fragment.sharedViewModel(viewModelFactory: ViewModelProvider.Factory? = null) =
  activity?.run { viewModel<FragmentActivity, T>(this, viewModelFactory) }
  ?: throw NullPointerException("Underlying activity is null")

inline fun <VH, reified T : ViewModel> viewModel(viewModelHolder: VH,
                                                 viewModelFactory: ViewModelProvider.Factory? = null) =
  when (viewModelHolder) {
    is Fragment         -> ViewModelProviders.of(viewModelHolder, viewModelFactory)
    is FragmentActivity -> ViewModelProviders.of(viewModelHolder, viewModelFactory)
    else                -> throw IllegalArgumentException("ViewModelHolder should be FragmentActivity or Fragment")
  }.get<T>()

inline fun <reified T : ViewModel> FragmentActivity.viewModel(crossinline factory: () -> T): T =
  viewModel(this, factory)

inline fun <reified T : ViewModel> Fragment.viewModel(crossinline factory: () -> T) = viewModel(this, factory)

inline fun <VH, reified T : ViewModel> viewModel(viewModelHolder: VH, crossinline factory: () -> T): T {
  @Suppress("UNCHECKED_CAST")
  val vmFactory = object : ViewModelProvider.Factory {
    override fun <U : ViewModel> create(modelClass: Class<U>): U = factory() as U
  }
  return viewModel(viewModelHolder, vmFactory)
}
