package com.releaseit.cryptoprices.details

import com.releaseit.cryptoprices.repository.Crypto
import com.releaseit.cryptoprices.repository.CryptoRepository
import com.releaseit.cryptoprices.repository.Currency
import com.releaseit.cryptoprices.utils.Prefs
import org.notests.rxfeedback.Optional
import org.notests.rxfeedback.SignalFeedback
import org.notests.rxfeedback.reactSafe
import org.notests.sharedsequence.Signal
import org.notests.sharedsequence.asSignal
import org.notests.sharedsequence.just
import java.net.UnknownHostException

/**
 * State, Events, automatic feedbacks
 */
data class State(val crypto: Optional<Crypto>,
                 val error: Optional<StateError>,
                 val loading: Boolean,
                 val currency: Currency) {
  companion object {
    val initial = State(Optional.None(), Optional.None(), true, Currency.USD)
  }
}

sealed class StateError {
  object NoInternet : StateError()
  object Unknown : StateError()
}

sealed class Event {
  data class CryptoLoaded(val crypto: Crypto) : Event()
  data class Error(val error: StateError) : Event()
  data class CurrencyChanged(val currency: Currency) : Event()
  object ReloadData : Event()
}

fun State.Companion.reduce(state: State, event: Event) =
  when (event) {
    is Event.CryptoLoaded    -> state.copy(crypto = Optional.Some(event.crypto),
                                           error = Optional.None(),
                                           loading = false)
    is Event.Error           -> state.copy(error = Optional.Some(event.error), loading = false)
    is Event.CurrencyChanged -> state.copy(currency = event.currency, loading = true)
    Event.ReloadData         -> state.copy(loading = true, error = Optional.None())
  }

// automatic feedback
fun CryptoRepository.feedback(id: String): SignalFeedback<State, Event> =
  reactSafe<State, Currency, Event>(
    query = {
      if (it.loading) return@reactSafe Optional.Some(it.currency)
      return@reactSafe Optional.None()
    },
    effects = {
      this.getCrypto(id, it)
        .toObservable()
        .map<Event> { Event.CryptoLoaded(it) }
        .asSignal<Event> {
          if (it is UnknownHostException) return@asSignal Signal.just(Event.Error(StateError.NoInternet))
          return@asSignal Signal.just(Event.Error(StateError.Unknown))
        }
    })

// automatic feedback
val Prefs.feedback: SignalFeedback<State, Event>
  get() = {
    this.currencyObservable
      .map<Event> { Event.CurrencyChanged(it) }
      .asSignal { Signal.just(Event.Error(StateError.Unknown)) }
  }
