package com.acme.transport.customs.broker.batch

import com.acme.transport.customs.clock.Clock

/** Moves legacy broker reservations through their life cycle and keeps a journal of what it did. */
data class LegacyBrokerReservationWorkflow(
    var store: LegacyBrokerReservationStore,
    var validator: LegacyBrokerReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyBrokerReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy broker reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyBrokerReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy broker reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyBrokerReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy broker reservation $code not found")
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

fun newLegacyBrokerReservationWorkflow(store: LegacyBrokerReservationStore, clock: Clock): LegacyBrokerReservationWorkflow {
    return LegacyBrokerReservationWorkflow(
        store = store,
        validator = defaultLegacyBrokerReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyBrokerReservationEngine(),
    )
}
