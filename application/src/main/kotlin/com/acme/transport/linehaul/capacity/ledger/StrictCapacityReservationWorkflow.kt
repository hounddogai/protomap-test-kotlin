package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.linehaul.clock.Clock

/** Moves strict capacity reservations through their life cycle and keeps a journal of what it did. */
data class StrictCapacityReservationService(
    var store: StrictCapacityReservationStore,
    var validator: StrictCapacityReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictCapacityReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict capacity reservation $code not found")
        validator.requireValid(item)
        item.moveTo(StrictCapacityReservationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict capacity reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictCapacityReservationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict capacity reservation $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newStrictCapacityReservationService(store: StrictCapacityReservationStore, clock: Clock): StrictCapacityReservationService {
    return StrictCapacityReservationService(
        store = store,
        validator = defaultStrictCapacityReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictCapacityReservationEngine(),
    )
}
