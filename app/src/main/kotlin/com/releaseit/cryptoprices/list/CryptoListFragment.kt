package com.releaseit.cryptoprices.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.releaseit.cryptoprices.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_crypto_list.*
import javax.inject.Inject


class CryptoListFragment : DaggerFragment() {

  companion object {
    fun newInstance(): Fragment = CryptoListFragment()
  }

  @Inject
  lateinit var viewModelFactory: CryptoListViewModelFactory

  private lateinit var viewModel: CryptoListViewModel

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel =
      ViewModelProviders.of(this, viewModelFactory)
        .get(CryptoListViewModel::class.java)
    viewModel.state.observe(this, Observer<State> { renderState(it) })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_crypto_list, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cryptoListFragmentswipeRefreshLayout.setOnRefreshListener { viewModel.reloadData() }
  }

  private fun renderState(state: State?) {
    if (state == null) return
    cryptoListFragmentswipeRefreshLayout.isRefreshing = false
  }

}
