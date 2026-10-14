package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves adaptive clearance reservations through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveClearanceReservationManager(
    var store: AdaptiveClearanceReservationStore,
    var validator: AdaptiveClearanceReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveClearanceReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive clearance reservation $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveClearanceReservationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive clearance reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveClearanceReservationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive clearance reservation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newAdaptiveClearanceReservationManager(store: AdaptiveClearanceReservationStore, clock: Clock, flags: Registry): AdaptiveClearanceReservationManager {
    return AdaptiveClearanceReservationManager(
        store = store,
        validator = defaultAdaptiveClearanceReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveClearanceReservationEngine(flags),
    )
}
