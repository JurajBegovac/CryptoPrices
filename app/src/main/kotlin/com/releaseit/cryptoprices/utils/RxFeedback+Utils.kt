package com.releaseit.cryptoprices.utils

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.PublishSubject
import org.notests.rxfeedback.SignalFeedback
import org.notests.rxfeedback.system
import org.notests.sharedsequence.*

/**
 * Created by jurajbegovac on 21/02/2018.
 */

abstract class RxFeedbackViewModel<State, in Event>(
        initialState: State,
        reducer: (State, Event) -> State,
        feedbacks: Iterable<SignalFeedback<State, Event>>) : ViewModel() {

    val state: Driver<State> =
            Driver.system(initialState, reducer, feedbacks.plus { uiEvents.asSignal { Signal.empty() } })

    private val uiEvents = PublishSubject.create<Event>()
    private var disposable: Disposable = Disposables.empty()

    init {
        disposable = state.drive()
    }

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }

    // This is a proxy for events that cannot be determine earlier (e.g. UI events)
    fun event(event: Event) {
        uiEvents.onNext(event)
    }
}