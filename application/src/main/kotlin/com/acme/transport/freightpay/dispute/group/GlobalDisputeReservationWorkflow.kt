package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global dispute reservations through their life cycle and keeps a journal of what it did. */
data class GlobalDisputeReservationCoordinator(
    var store: GlobalDisputeReservationStore,
    var validator: GlobalDisputeReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDisputeReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global dispute reservation $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDisputeReservationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global dispute reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDisputeReservationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global dispute reservation $code not found")
        val result = engine.price(item)
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

fun newGlobalDisputeReservationCoordinator(store: GlobalDisputeReservationStore, clock: Clock, flags: Registry): GlobalDisputeReservationCoordinator {
    return GlobalDisputeReservationCoordinator(
        store = store,
        validator = defaultGlobalDisputeReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDisputeReservationEngine(flags),
    )
}
