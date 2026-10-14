package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.linehaul.clock.Clock

/** Moves legacy backhaul reservations through their life cycle and keeps a journal of what it did. */
data class LegacyBackhaulReservationService(
    var store: LegacyBackhaulReservationStore,
    var validator: LegacyBackhaulReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyBackhaulReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy backhaul reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyBackhaulReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy backhaul reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyBackhaulReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy backhaul reservation $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLegacyBackhaulReservationService(store: LegacyBackhaulReservationStore, clock: Clock): LegacyBackhaulReservationService {
    return LegacyBackhaulReservationService(
        store = store,
        validator = defaultLegacyBackhaulReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyBackhaulReservationEngine(),
    )
}
