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
class CryptoListTest : Assert() {

  @Test
  fun testReducer_dataLoaded_initialState() {
    val initialState = State.initial()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    assertEquals(items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_dataLoaded_loadedState() {
    val initialState = State.loadedWithItems()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    assertEquals(items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_dataLoaded_loadingState() {
    val initialState = State.loading()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    assertEquals(items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_noInternetError_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_noInternetError_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_noInternetError_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_unknownError_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_unknownError_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_unknownError_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    assertEquals(false, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_changeCurrency_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.CurrencyChanged(Currency.EUR))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
    assertEquals(Currency.EUR, loadedState.currency)
  }

  @Test
  fun testReducer_changeCurrency_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.CurrencyChanged(Currency.EUR))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
    assertEquals(Currency.EUR, loadedState.currency)
  }

  @Test
  fun testReducer_changeCurrency_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.CurrencyChanged(Currency.EUR))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
    assertEquals(Currency.EUR, loadedState.currency)
  }

  @Test
  fun testReducer_reloadData_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_reloadData_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
  }

  @Test
  fun testReducer_reloadData_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
    assertEquals(initialState.currency, loadedState.currency)
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
