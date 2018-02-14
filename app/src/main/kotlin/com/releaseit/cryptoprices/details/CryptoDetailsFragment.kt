package com.releaseit.cryptoprices.details

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.utils.inflate
import com.releaseit.cryptoprices.utils.showToast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_crypto_details.*
import kotlinx.android.synthetic.main.item_crypto_details.view.*
import javax.inject.Inject

/**
 * Created by jurajbegovac on 13/02/2018.
 */
class CryptoDetailsFragment : DaggerFragment() {

  companion object {
    const val KEY_ID = "key:id"
    fun newInstance(id: String): Fragment {
      val cryptoDetailsFragment = CryptoDetailsFragment()
      val bundle = Bundle()
      bundle.putString(KEY_ID, id)
      cryptoDetailsFragment.arguments = bundle
      return cryptoDetailsFragment
    }
  }

  @Inject
  lateinit var repository: CryptoRepository

  @Inject
  lateinit var sharedPreferences: SharedPreferences

  private lateinit var viewModel: CryptoDetailsViewModel

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel =
      ViewModelProviders.of(this,
                            CryptoDetailsViewModelFactory(arguments.getString(KEY_ID)!!, repository, sharedPreferences))
        .get(CryptoDetailsViewModel::class.java)
    viewModel.state.observe(this, Observer<State> { renderState(it) })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_crypto_details, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cryptoDetailsFragmentSwipeRefreshLayout.setOnRefreshListener { viewModel.reloadData() }
    cryptoDetailsFragmentRecylerView.apply {
      layoutManager = LinearLayoutManager(context)
    }
  }

  private fun renderState(state: State?) {
    if (state == null) return

    // show loading if needed
    if (cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing != state.showLoading)
      cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing = state.showLoading

    // show data
    val crypto = state.crypto
    if (crypto != null) {
      cryptoDetailsFragmentRecylerView.adapter = CryptoAdapter(crypto.detailItems)
    }

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
internal val Crypto.detailItems: List<CryptoDetailItem>
  get() = listOf(
    CryptoDetailItem(R.string.crypto_detail_name, name),
    CryptoDetailItem(R.string.crypto_detail_rank, rank),
    CryptoDetailItem(R.string.crypto_detail_symbol, symbol),
    CryptoDetailItem(R.string.crypto_detail_price, "$price $currency"),
    CryptoDetailItem(R.string.crypto_detail_24h_volume, "$_24hVolume $currency"),
    CryptoDetailItem(R.string.crypto_detail_market_cap, "$marketCap $currency"),
    CryptoDetailItem(R.string.crypto_detail_price_btc, priceBtc),
    CryptoDetailItem(R.string.crypto_detail_1h_change, "$percentChange1h%"),
    CryptoDetailItem(R.string.crypto_detail_24h_change, "$percentChange24h%"),
    CryptoDetailItem(R.string.crypto_detail_7d_change, "$percentChange7d%"),
    CryptoDetailItem(R.string.crypto_detail_total_supply, totalSupply),
    CryptoDetailItem(R.string.crypto_detail_available_supply, availableSupply))

/**
 * List item
 */
data class CryptoDetailItem(@StringRes val nameResId: Int, val value: String)

/**
 * Recyclerview adapter
 */
internal class CryptoAdapter(private val items: List<CryptoDetailItem>)
  : RecyclerView.Adapter<CryptoDetailsViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    CryptoDetailsViewHolder(parent.inflate(R.layout.item_crypto_details))

  override fun getItemCount() = items.count()

  override fun onBindViewHolder(holder: CryptoDetailsViewHolder?, position: Int) {
    holder?.bind(items[position])
  }
}

internal class CryptoDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(item: CryptoDetailItem) {
    itemView.itemCryptoDetailName.setText(item.nameResId)
    itemView.itemCryptoDetailValue.text = item.value
  }
}
