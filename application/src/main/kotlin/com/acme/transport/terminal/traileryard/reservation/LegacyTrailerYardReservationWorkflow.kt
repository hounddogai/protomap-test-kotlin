package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves legacy trailer yard reservations through their life cycle and keeps a journal of what it did.
 */
data class LegacyTrailerYardReservationWorkflow(
    var store: LegacyTrailerYardReservationStore,
    var validator: LegacyTrailerYardReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyTrailerYardReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy trailer yard reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTrailerYardReservationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy trailer yard reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTrailerYardReservationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy trailer yard reservation $code not found")
        val result = engine.price(item)
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

fun newLegacyTrailerYardReservationWorkflow(store: LegacyTrailerYardReservationStore, clock: Clock, flags: Registry): LegacyTrailerYardReservationWorkflow {
    return LegacyTrailerYardReservationWorkflow(
        store = store,
        validator = defaultLegacyTrailerYardReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyTrailerYardReservationEngine(flags),
    )
}
