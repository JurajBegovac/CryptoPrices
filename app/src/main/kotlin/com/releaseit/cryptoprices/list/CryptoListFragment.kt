package com.releaseit.cryptoprices.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.utils.inflate
import com.releaseit.cryptoprices.utils.showToast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_crypto_list.*
import kotlinx.android.synthetic.main.item_crypto.view.*
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
    cryptoListFragmentRecylerView.apply {
      layoutManager = LinearLayoutManager(context)
    }
  }

  private fun renderState(state: State?) {
    if (state == null) return

    // show loading if needed
    if (cryptoListFragmentswipeRefreshLayout.isRefreshing != state.showLoading)
      cryptoListFragmentswipeRefreshLayout.isRefreshing = state.showLoading

    // show data
    cryptoListFragmentRecylerView.adapter = CryptoAdapter(
      { state.items[it].listItem },
      { state.items.count() },
      { context.showToast("Item pressed at position: $it") })

    // show error
    when (state.error) {
      StateError.NoInternet -> context.showToast(R.string.error_no_internet)
      StateError.Unknown    -> context.showToast(R.string.error_unknown)
    }
  }
}

/**
 * Mapper from Crypto to CryptoListItem
 */
internal val Crypto.listItem: CryptoListItem
  get() = CryptoListItem(rank, symbol, price, _24hVolume)

/**
 * List item
 */
data class CryptoListItem(val rank: String, val symbol: String, val price: String, val volume24h: String)

/**
 * Recyclerview adapter
 */
internal class CryptoAdapter(private val itemProvider: (Int) -> CryptoListItem, private val itemCount: () -> Int,
                             private val clickListener: (Int) -> (Unit))
  : RecyclerView.Adapter<CryptoViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    CryptoViewHolder(parent.inflate(R.layout.item_crypto))

  override fun getItemCount() = itemCount()

  override fun onBindViewHolder(holder: CryptoViewHolder?, position: Int) {
    holder?.bind(itemProvider(position), { clickListener(position) })
  }
}

internal class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(item: CryptoListItem, clickListener: () -> Unit) {
    itemView.itemCryptoRoot.setOnClickListener { clickListener() }
    itemView.itemCryptoRank.text = item.rank
    itemView.itemCryptoSymbol.text = item.symbol
    itemView.itemCryptoPrice.text = item.price
    itemView.itemCryptoVolume24h.text = item.volume24h
  }
}
