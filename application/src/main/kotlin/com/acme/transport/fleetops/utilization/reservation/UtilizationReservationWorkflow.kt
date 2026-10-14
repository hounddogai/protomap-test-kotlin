package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization reservations through their life cycle and keeps a journal of what it did. */
data class UtilizationReservationCoordinator(
    var store: UtilizationReservationStore,
    var validator: UtilizationReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("utilization reservation $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationReservationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationReservationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("utilization reservation $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newUtilizationReservationCoordinator(store: UtilizationReservationStore, clock: Clock): UtilizationReservationCoordinator {
    return UtilizationReservationCoordinator(
        store = store,
        validator = defaultUtilizationReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationReservationEngine(),
    )
}
