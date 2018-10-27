package com.releaseit.cryptoprices.list

import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.Currency
import org.junit.Assert
import org.junit.Test
import org.notests.rxfeedback.Optional
import java.util.*

class CryptoListTest : Assert() {

  @Test
  fun testReducer_dataLoaded_initialState() {
    val initialState = State.initial()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    assertEquals(items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_dataLoaded_loadedState() {
    val initialState = State.loadedWithItems()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    assertEquals(items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_dataLoaded_loadingState() {
    val initialState = State.loading()
    val items = dummyItems()
    val loadedState = State.reduce(initialState, Event.DataLoaded(items))

    assertEquals(items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_noInternetError_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_noInternetError_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_noInternetError_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.Error(StateError.NoInternet))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.NoInternet), loadedState.error)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_unknownError_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_unknownError_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_unknownError_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.Error(StateError.Unknown))

    assertEquals(initialState.items, loadedState.items)
    assertEquals(Optional.Some(StateError.Unknown), loadedState.error)
    assertEquals(false, loadedState.loading)
  }

  @Test
  fun testReducer_reloadData_initialState() {
    val initialState = State.initial()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
  }

  @Test
  fun testReducer_reloadData_loadedState() {
    val initialState = State.loadedWithItems()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
  }

  @Test
  fun testReducer_reloadData_loadingState() {
    val initialState = State.loading()
    val loadedState = State.reduce(initialState, Event.ReloadData)

    assertEquals(initialState.items, loadedState.items)
    assertEquals(true, loadedState.error is Optional.None)
    assertEquals(true, loadedState.loading)
  }

  private fun State.Companion.loadedWithItems() = State(dummyItems(), Optional.None(), false)
  private fun State.Companion.loading() = State(emptyList(), Optional.None(), true)

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

}
