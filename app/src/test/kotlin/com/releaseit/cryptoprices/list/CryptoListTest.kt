package com.releaseit.cryptoprices.list

import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.Currency
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.notests.rxfeedback.Optional
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class CryptoListTest {

  @Test
  fun testReducer_dataLoaded_initialState() {
    val initialState = State.initial()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    Assert.assertEquals(items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_dataLoaded_loadedState() {
    val initialState = State.loadedWithItems()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    Assert.assertEquals(items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_dataLoaded_loadingState() {
    val initialState = State.loading()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    Assert.assertEquals(items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_noInternetError_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_noInternetError_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_noInternetError_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_unknownError_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_unknownError_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_unknownError_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    Assert.assertEquals(false, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_changeCurrency_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.CurrencyChanged(Currency.EUR))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(true, loadedState.loading)
    Assert.assertEquals(Currency.EUR, loadedState.currency)
  }

  @Test
  fun testReducer_changeCurrency_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.CurrencyChanged(Currency.EUR))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(true, loadedState.loading)
    Assert.assertEquals(Currency.EUR, loadedState.currency)
  }

  @Test
  fun testReducer_changeCurrency_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.CurrencyChanged(Currency.EUR))

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(true, loadedState.loading)
    Assert.assertEquals(Currency.EUR, loadedState.currency)
  }

  @Test
  fun testReducer_reloadData_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(true, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_reloadData_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(true, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_reloadData_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    Assert.assertEquals(initialState.items, loadedState.items)
    Assert.assertEquals(true, loadedState.error is Optional.None)
    Assert.assertEquals(true, loadedState.loading)
    Assert.assertEquals(initialState.currency, loadedState.currency)
  }

}

private fun State.Companion.loadedWithItems() = State(dummyItems(), Optional.None(), false, Currency.USD)
private fun State.Companion.loading() = State(emptyList(), Optional.None(), true, Currency.USD)

private fun dummyItems(): List<Crypto> {
  var items: List<Crypto> = ArrayList()
  val random = Random()
  for (i in 1..20) {
    items = items.plus(Crypto("${random.nextInt()}",
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
