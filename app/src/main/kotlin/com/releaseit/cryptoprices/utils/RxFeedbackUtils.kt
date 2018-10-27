package com.releaseit.cryptoprices.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.PublishSubject
import org.notests.rxfeedback.Bindings
import org.notests.rxfeedback.SignalFeedback
import org.notests.rxfeedback.bindSafe
import org.notests.rxfeedback.system
import org.notests.sharedsequence.Driver
import org.notests.sharedsequence.Signal
import org.notests.sharedsequence.asSignal
import org.notests.sharedsequence.drive
import org.notests.sharedsequence.empty

/**
 * Created by jurajbegovac on 21/02/2018.
 */

class RxFeedbackViewModel<State, in Event>(
  initialState: State, reducer: (State, Event) -> State, feedbacks:
  Iterable<SignalFeedback<State, Event>>) : ViewModel() {

  val state: Driver<State> =
    Driver.system(initialState, reducer, feedbacks.plus { _ -> uiEvents.asSignal { Signal.empty() } })

  private val uiEvents = PublishSubject.create<Event>()
  private var disposable: Disposable = Disposables.empty()

  init {
    disposable = state.drive()
  }

  override fun onCleared() {
    disposable.dispose()
    super.onCleared()
  }


  // This is a proxy for events that cannot be determined earlier (e.g. UI events)
  fun event(event: Event) {
    uiEvents.onNext(event)
  }
}

typealias UIBinding<State, Event> = (Driver<State>) -> (Bindings<Event>)

interface RxFeedbackView<State, Event> : UIBinding<State, Event>, LifecycleOwner

fun <State, Event> RxFeedbackViewModel<State, Event>.bindUI(view: RxFeedbackView<State, Event>) {
  view.lifecycle.addObserver(object : LifecycleObserver {
    private var disposable = Disposables.empty()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun subscribe() {
      disposable = bindSafe(view).invoke(state)
        .asObservable()
        .subscribe { this@bindUI.event(it) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun dispose() {
      disposable.dispose()
    }
  })
}
