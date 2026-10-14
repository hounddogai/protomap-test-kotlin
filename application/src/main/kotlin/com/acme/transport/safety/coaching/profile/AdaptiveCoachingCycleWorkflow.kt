package com.acme.transport.safety.coaching.profile

import com.acme.transport.safety.clock.Clock

/** Moves adaptive coaching cycles through their life cycle and keeps a journal of what it did. */
data class AdaptiveCoachingCycleCoordinator(
    var store: AdaptiveCoachingCycleStore,
    var validator: AdaptiveCoachingCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive coaching cycle $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveCoachingCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive coaching cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveCoachingCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newAdaptiveCoachingCycleCoordinator(store: AdaptiveCoachingCycleStore, clock: Clock): AdaptiveCoachingCycleCoordinator {
    return AdaptiveCoachingCycleCoordinator(
        store = store,
        validator = defaultAdaptiveCoachingCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
