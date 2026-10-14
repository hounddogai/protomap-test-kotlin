package com.acme.transport.customs.broker.metric

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary broker reservations through their life cycle and keeps a journal of what it did. */
data class PrimaryBrokerReservationDesk(
    var store: PrimaryBrokerReservationStore,
    var validator: PrimaryBrokerReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryBrokerReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary broker reservation $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryBrokerReservationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary broker reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryBrokerReservationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary broker reservation $code not found")
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

fun newPrimaryBrokerReservationDesk(store: PrimaryBrokerReservationStore, clock: Clock, flags: Registry): PrimaryBrokerReservationDesk {
    return PrimaryBrokerReservationDesk(
        store = store,
        validator = defaultPrimaryBrokerReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryBrokerReservationEngine(flags),
    )
}
