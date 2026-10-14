package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute reservations through their life cycle and keeps a journal of what it did. */
data class DisputeReservationService(
    var store: DisputeReservationStore,
    var validator: DisputeReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dispute reservation $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeReservationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeReservationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("dispute reservation $code not found")
        val result = engine.compute(item)
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

fun newDisputeReservationService(store: DisputeReservationStore, clock: Clock, flags: Registry): DisputeReservationService {
    return DisputeReservationService(
        store = store,
        validator = defaultDisputeReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeReservationEngine(flags),
    )
}
