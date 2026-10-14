package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves default sortation reservations through their life cycle and keeps a journal of what it did.
 */
data class DefaultSortationReservationWorkflow(
    var store: DefaultSortationReservationStore,
    var validator: DefaultSortationReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultSortationReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default sortation reservation $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultSortationReservationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default sortation reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultSortationReservationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default sortation reservation $code not found")
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

fun newDefaultSortationReservationWorkflow(store: DefaultSortationReservationStore, clock: Clock, flags: Registry): DefaultSortationReservationWorkflow {
    return DefaultSortationReservationWorkflow(
        store = store,
        validator = defaultDefaultSortationReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultSortationReservationEngine(flags),
    )
}
