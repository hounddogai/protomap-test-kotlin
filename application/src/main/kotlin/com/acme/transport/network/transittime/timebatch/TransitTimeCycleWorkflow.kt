package com.acme.transport.network.transittime.timebatch

import com.acme.transport.network.clock.Clock

/** Moves transit time cycles through their life cycle and keeps a journal of what it did. */
data class TransitTimeCycleCoordinator(
    var store: TransitTimeCycleStore,
    var validator: TransitTimeCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("transit time cycle $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newTransitTimeCycleCoordinator(store: TransitTimeCycleStore, clock: Clock): TransitTimeCycleCoordinator {
    return TransitTimeCycleCoordinator(
        store = store,
        validator = defaultTransitTimeCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
