package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.lastmile.clock.Clock

/** Moves drop reservations through their life cycle and keeps a journal of what it did. */
data class DropReservationService(
    var store: DropReservationStore,
    var validator: DropReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("drop reservation $code not found")
        validator.requireValid(item)
        item.moveTo(DropReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("drop reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("drop reservation $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newDropReservationService(store: DropReservationStore, clock: Clock): DropReservationService {
    return DropReservationService(
        store = store,
        validator = defaultDropReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropReservationEngine(),
    )
}
