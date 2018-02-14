package com.releaseit.cryptoprices.list

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.Currency
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.SchedulerProvider
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit


/**
 * Created by jurajbegovac on 14/02/2018.
 */
@RunWith(MockitoJUnitRunner::class)
class CryptoListViewModelTest {

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  private lateinit var cryptoListViewModel: CryptoListViewModel

  private var repository: CryptoRepository = object : CryptoRepository {
    override fun getCryptos(currency: Currency, limit: String) = Single.just(dummyItems)
    override fun getCrypto(id: String, currency: Currency) = Single.just(dummyItems.first())
  }

  private val prefs: Prefs = object : Prefs {
    override var currency: Currency
      get() = Currency.USD
      set(value) {}
    override val currencyObservable: Observable<Currency>
      get() = Observable.just(Currency.USD)
  }

  private val schedulerProvider: SchedulerProvider = object : SchedulerProvider {
    override fun io(): Scheduler = testScheduler
    override fun main(): Scheduler = testScheduler
  }

  private val testScheduler = TestScheduler()

  @Mock
  private lateinit var observer: Observer<State>

  @Before
  fun setup() {
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun testSuccess() {
    cryptoListViewModel = CryptoListViewModel(repository, prefs, schedulerProvider)
    cryptoListViewModel.state.observeForever(observer)

    Assert.assertEquals(cryptoListViewModel.state.value, State.initial())

    testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

    Assert.assertEquals(cryptoListViewModel.state.value, State(dummyItems, null, false))
  }

  @Test
  fun testNoInternet() {
    repository = object : CryptoRepository {
      override fun getCryptos(currency: Currency, limit: String) = Single.error<List<Crypto>>(UnknownHostException())
      override fun getCrypto(id: String, currency: Currency) = Single.error<Crypto>(UnknownHostException())
    }
    cryptoListViewModel = CryptoListViewModel(repository, prefs, schedulerProvider)

    cryptoListViewModel.state.observeForever(observer)

    Assert.assertEquals(cryptoListViewModel.state.value, State.initial())

    testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

    Assert.assertEquals(cryptoListViewModel.state.value, State(emptyList(), StateError.NoInternet, false))
  }

  @Test
  fun testUnknownException() {
    repository = object : CryptoRepository {
      override fun getCryptos(currency: Currency, limit: String) = Single.error<List<Crypto>>(RuntimeException())
      override fun getCrypto(id: String, currency: Currency) = Single.error<Crypto>(RuntimeException())
    }
    cryptoListViewModel = CryptoListViewModel(repository, prefs, schedulerProvider)

    cryptoListViewModel.state.observeForever(observer)

    Assert.assertEquals(cryptoListViewModel.state.value, State.initial())

    testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

    Assert.assertEquals(cryptoListViewModel.state.value, State(emptyList(), StateError.Unknown, false))
  }
}

val dummyItems: List<Crypto> = generateDummyItems()

fun generateDummyItems(): List<Crypto> {
  var items: List<Crypto> = ArrayList()
  for (i in 1..20) {
    items = items.plus(Crypto("$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              "$i",
                              Currency.USD,
                              "$i"))
  }
  return items
}
