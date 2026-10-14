package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay reservations through their life cycle and keeps a journal of what it did. */
data class RelayReservationCoordinator(
    var store: RelayReservationStore,
    var validator: RelayReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("relay reservation $code not found")
        validator.requireValid(item)
        item.moveTo(RelayReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("relay reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("relay reservation $code not found")
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

fun newRelayReservationCoordinator(store: RelayReservationStore, clock: Clock, flags: Registry): RelayReservationCoordinator {
    return RelayReservationCoordinator(
        store = store,
        validator = defaultRelayReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayReservationEngine(flags),
    )
}
