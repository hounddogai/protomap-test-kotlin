package com.acme.transport.crewing.availability.reservation

import com.acme.transport.crewing.clock.Clock

/** Moves availability reservations through their life cycle and keeps a journal of what it did. */
data class AvailabilityReservationManager(
    var store: AvailabilityReservationStore,
    var validator: AvailabilityReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AvailabilityReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("availability reservation $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("availability reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("availability reservation $code not found")
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

fun newAvailabilityReservationManager(store: AvailabilityReservationStore, clock: Clock): AvailabilityReservationManager {
    return AvailabilityReservationManager(
        store = store,
        validator = defaultAvailabilityReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAvailabilityReservationEngine(),
    )
}
