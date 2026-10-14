package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves scheduled transit time batches through their life cycle and keeps a journal of what it did.
 */
data class ScheduledTransitTimeBatchCoordinator(
    var store: ScheduledTransitTimeBatchStore,
    var validator: ScheduledTransitTimeBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledTransitTimeBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled transit time batch $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTransitTimeBatchStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled transit time batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTransitTimeBatchStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled transit time batch $code not found")
        val result = engine.compute(item)
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

fun newScheduledTransitTimeBatchCoordinator(store: ScheduledTransitTimeBatchStore, clock: Clock, flags: Registry): ScheduledTransitTimeBatchCoordinator {
    return ScheduledTransitTimeBatchCoordinator(
        store = store,
        validator = defaultScheduledTransitTimeBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledTransitTimeBatchEngine(flags),
    )
}
