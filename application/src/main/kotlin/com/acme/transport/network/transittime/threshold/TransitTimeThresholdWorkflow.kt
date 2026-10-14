package com.acme.transport.network.transittime.threshold

import com.acme.transport.network.clock.Clock

/** Moves transit time thresholds through their life cycle and keeps a journal of what it did. */
data class TransitTimeThresholdCoordinator(
    var store: TransitTimeThresholdStore,
    var validator: TransitTimeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("transit time threshold $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newTransitTimeThresholdCoordinator(store: TransitTimeThresholdStore, clock: Clock): TransitTimeThresholdCoordinator {
    return TransitTimeThresholdCoordinator(
        store = store,
        validator = defaultTransitTimeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
