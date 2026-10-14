package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves scheduled sortation baselines through their life cycle and keeps a journal of what it did. */
data class ScheduledSortationBaselineDesk(
    var store: ScheduledSortationBaselineStore,
    var validator: ScheduledSortationBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledSortationBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled sortation baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledSortationBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled sortation baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledSortationBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled sortation baseline $code not found")
        val result = engine.run(item)
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

fun newScheduledSortationBaselineDesk(store: ScheduledSortationBaselineStore, clock: Clock, flags: Registry): ScheduledSortationBaselineDesk {
    return ScheduledSortationBaselineDesk(
        store = store,
        validator = defaultScheduledSortationBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledSortationBaselineEngine(flags),
    )
}
