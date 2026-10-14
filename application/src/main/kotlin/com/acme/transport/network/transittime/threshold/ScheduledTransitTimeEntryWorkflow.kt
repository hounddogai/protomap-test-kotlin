package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves scheduled transit time entries through their life cycle and keeps a journal of what it did.
 */
data class ScheduledTransitTimeEntryCoordinator(
    var store: ScheduledTransitTimeEntryStore,
    var validator: ScheduledTransitTimeEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledTransitTimeEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled transit time entry $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTransitTimeEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled transit time entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTransitTimeEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled transit time entry $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newScheduledTransitTimeEntryCoordinator(store: ScheduledTransitTimeEntryStore, clock: Clock, flags: Registry): ScheduledTransitTimeEntryCoordinator {
    return ScheduledTransitTimeEntryCoordinator(
        store = store,
        validator = defaultScheduledTransitTimeEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledTransitTimeEntryEngine(flags),
    )
}
