/*
 * Copyright © 2014-2018, TWINT AG.
 * All rights reserved.
 */

package com.releaseit.cryptoprices.details

import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.utils.Prefs
import com.releaseit.cryptoprices.utils.RxFeedbackViewModel
import org.notests.rxfeedback.Optional
import org.notests.rxfeedback.SignalFeedback
import org.notests.rxfeedback.reactSafe
import org.notests.sharedsequence.Driver
import org.notests.sharedsequence.Signal
import org.notests.sharedsequence.asSignal
import org.notests.sharedsequence.distinctUntilChanged
import org.notests.sharedsequence.filter
import org.notests.sharedsequence.just
import org.notests.sharedsequence.map
import java.net.UnknownHostException

/**
 * ViewModel
 */
typealias CryptoDetailsViewModel = RxFeedbackViewModel<State, Event>

/**
 * State
 */
data class State(val crypto: Optional<Crypto>,
                 val error: Optional<StateError>,
                 val loading: Boolean) {
  companion object {
    val initial = State(Optional.None(), Optional.None(), true)
  }
}

sealed class StateError {
  object NoInternet : StateError()
  object Unknown : StateError()
}

/**
 * Events
 */
sealed class Event {
  data class CryptoLoaded(val crypto: Crypto) : Event()
  data class Error(val error: StateError) : Event()
  object ReloadData : Event()
}

/**
 * Reducer
 */
fun State.Companion.reduce(state: State, event: Event) =
  when (event) {
    is Event.CryptoLoaded -> state.copy(crypto = Optional.Some(event.crypto),
                                        error = Optional.None(),
                                        loading = false)
    is Event.Error        -> state.copy(error = Optional.Some(event.error), loading = false)
    Event.ReloadData      -> state.copy(loading = true, error = Optional.None())
  }

/**
 * Feedbacks
 */

val Prefs.currencyDetailsFeedback: SignalFeedback<State, Event>
  get() = { _ ->
    currencyObservable
      .distinctUntilChanged()
      .map<Event> { Event.ReloadData }
      .asSignal { Signal.just(Event.Error(StateError.Unknown)) }
  }

fun loadingFeedback(cryptoRepository: CryptoRepository,
                    prefs: Prefs,
                    id: String): SignalFeedback<State, Event> =
  reactSafe<State, String, Event>(
    query = {
      if (it.loading) Optional.Some(id)
      else Optional.None()
    },
    effects = { cryptoId ->
      prefs.currencyObservable
        .distinctUntilChanged()
        .flatMapSingle { cryptoRepository.getCrypto(cryptoId, it) }
        .map<Event> { Event.CryptoLoaded(it) }
        .asSignal<Event> {
          if (it is UnknownHostException) Signal.just(Event.Error(StateError.NoInternet))
          else Signal.just(Event.Error(StateError.Unknown))
        }
    })

/**
 * Mappers
 */
val Driver<State>.crypto
  get() = map { it.crypto }.filter { it is Optional.Some }.map { (it as Optional.Some).data }.distinctUntilChanged()

val Driver<State>.load
  get() = map { it.loading }.distinctUntilChanged()

val Driver<State>.error
  get() = map { it.error }.filter { it !is Optional.None }.map { (it as Optional.Some).data }
