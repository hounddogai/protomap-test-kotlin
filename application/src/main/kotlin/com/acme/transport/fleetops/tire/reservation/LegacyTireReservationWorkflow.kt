package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy tire reservations through their life cycle and keeps a journal of what it did. */
data class LegacyTireReservationWorkflow(
    var store: LegacyTireReservationStore,
    var validator: LegacyTireReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyTireReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy tire reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTireReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy tire reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTireReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy tire reservation $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newLegacyTireReservationWorkflow(store: LegacyTireReservationStore, clock: Clock, flags: Registry): LegacyTireReservationWorkflow {
    return LegacyTireReservationWorkflow(
        store = store,
        validator = defaultLegacyTireReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyTireReservationEngine(flags),
    )
}
