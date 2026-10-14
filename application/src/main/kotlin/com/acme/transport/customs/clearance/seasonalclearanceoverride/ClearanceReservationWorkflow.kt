package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.customs.clock.Clock

/** Moves clearance reservations through their life cycle and keeps a journal of what it did. */
data class ClearanceReservationCoordinator(
    var store: ClearanceReservationStore,
    var validator: ClearanceReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("clearance reservation $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("clearance reservation $code not found")
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

fun newClearanceReservationCoordinator(store: ClearanceReservationStore, clock: Clock): ClearanceReservationCoordinator {
    return ClearanceReservationCoordinator(
        store = store,
        validator = defaultClearanceReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceReservationEngine(),
    )
}
