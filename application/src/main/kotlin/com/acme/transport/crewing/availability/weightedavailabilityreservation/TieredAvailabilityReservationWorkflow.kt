package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.crewing.clock.Clock

/**
 * Moves tiered availability reservations through their life cycle and keeps a journal of what it did.
 */
data class TieredAvailabilityReservationDesk(
    var store: TieredAvailabilityReservationStore,
    var validator: TieredAvailabilityReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredAvailabilityReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered availability reservation $code not found")
        validator.requireValid(item)
        item.moveTo(TieredAvailabilityReservationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered availability reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredAvailabilityReservationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered availability reservation $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newTieredAvailabilityReservationDesk(store: TieredAvailabilityReservationStore, clock: Clock): TieredAvailabilityReservationDesk {
    return TieredAvailabilityReservationDesk(
        store = store,
        validator = defaultTieredAvailabilityReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredAvailabilityReservationEngine(),
    )
}
