package com.releaseit.cryptoprices.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.navigation.Screen
import com.releaseit.cryptoprices.navigation.navigation
import com.releaseit.cryptoprices.utils.bindUI
import com.releaseit.cryptoprices.utils.inflate
import com.releaseit.cryptoprices.utils.showToast
import com.releaseit.cryptoprices.utils.viewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_crypto_details.*
import kotlinx.android.synthetic.main.item_crypto_details.view.*
import javax.inject.Inject

class CryptoDetailsFragment : DaggerFragment(), CryptoDetailsView {

  @Inject
  lateinit var viewModelFactory: CryptoDetailsViewModelFactory

  companion object {
    const val KEY_ID = "CryptoDetailsFragment:key:id"
    fun newInstance(id: String) = CryptoDetailsFragment().apply {
      arguments = bundleOf(KEY_ID to id)
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    viewModel(viewModelFactory).bindUI(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.fragment_crypto_details, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cryptoDetailsFragmentRecylerView.layoutManager = LinearLayoutManager(context)
    cryptoDetailsFragmentToolbar.apply {
      inflateMenu(R.menu.menu_main)
      setOnMenuItemClickListener { menuItem ->
        menuItem.takeIf { it.itemId == R.id.menu_action_settings }?.let { navigation.to(Screen.Settings); true }
        ?: false
      }
      setNavigationOnClickListener { navigation.back() }
    }
  }

  override val swipeRefreshLayout: SwipeRefreshLayout
    get() = cryptoDetailsFragmentSwipeRefreshLayout

  override fun showLoading(loading: Boolean) {
    if (loading != cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing) {
      cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing = loading
    }
  }

  override fun showError(@StringRes errorResId: Int) {
    context?.showToast(errorResId)
  }

  override fun setTitle(title: String) {
    cryptoDetailsFragmentToolbar.title = title
  }

  override fun showDetails(details: List<CryptoDetailItem>) {
    cryptoDetailsFragmentRecylerView.adapter = CryptoAdapter(details)
  }
}

private class CryptoAdapter(private val items: List<CryptoDetailItem>) :
  RecyclerView.Adapter<CryptoDetailsViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    CryptoDetailsViewHolder(parent.inflate(R.layout.item_crypto_details))

  override fun getItemCount() = items.count()

  override fun onBindViewHolder(holder: CryptoDetailsViewHolder, position: Int) {
    holder.bind(items[position])
  }
}

private class CryptoDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(item: CryptoDetailItem) {
    itemView.itemCryptoDetailName.setText(item.nameResId)
    itemView.itemCryptoDetailValue.text = item.value
  }
}
