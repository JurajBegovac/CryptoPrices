package com.releaseit.cryptoprices.details

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.releaseit.cryptoprices.R
import com.releaseit.cryptoprices.utils.inflate
import com.releaseit.cryptoprices.utils.showToast
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.fragment_crypto_details.*
import kotlinx.android.synthetic.main.item_crypto_details.view.*
import org.notests.sharedsequence.Driver
import org.notests.sharedsequence.drive
import javax.inject.Inject

/**
 * Created by $USER_NAME on 14/06/2018.
 */
class CryptoDetailsFragment : DaggerFragment(), CryptoDetailsView {

  companion object {
    const val KEY_ID = "CryptoDetailsFragment:key:id"
    fun newInstance(id: String): Fragment {
      val cryptoDetailsFragment = CryptoDetailsFragment()
      val bundle = Bundle()
      bundle.putString(KEY_ID, id)
      cryptoDetailsFragment.arguments = bundle
      return cryptoDetailsFragment
    }
  }

  @Inject
  lateinit var state: Driver<State>

  private var disposable = Disposables.empty()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
    inflater.inflate(R.layout.fragment_crypto_details, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    cryptoDetailsFragmentRecylerView.layoutManager = LinearLayoutManager(context)
    cryptoDetailsFragmentToolbar.inflateMenu(R.menu.menu_main)
  }

  override fun onStart() {
    super.onStart()
    disposable = state.drive()
  }

  override fun onStop() {
    disposable.dispose()
    super.onStop()
  }

  override fun showLoading(loading: Boolean) {
    if (loading != cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing) {
      cryptoDetailsFragmentSwipeRefreshLayout.isRefreshing = loading
    }
  }

  override fun showTitle(title: String) {
    cryptoDetailsFragmentToolbar.title = title
  }

  override fun showCryptoDetails(details: List<CryptoDetailItem>) {
    cryptoDetailsFragmentRecylerView.adapter = CryptoAdapter(details)
  }

  override fun showError(errorMsgId: Int) {
    context?.showToast(errorMsgId)
  }

  override val swipeRefreshLayout: SwipeRefreshLayout
    get() = cryptoDetailsFragmentSwipeRefreshLayout

  override val toolbar: Toolbar
    get() = cryptoDetailsFragmentToolbar
}

/**
 * List item
 */
data class CryptoDetailItem(@StringRes val nameResId: Int, val value: String)

/**
 * Recyclerview adapter
 */
private class CryptoAdapter(private val items: List<CryptoDetailItem>)
  : RecyclerView.Adapter<CryptoDetailsViewHolder>() {

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
