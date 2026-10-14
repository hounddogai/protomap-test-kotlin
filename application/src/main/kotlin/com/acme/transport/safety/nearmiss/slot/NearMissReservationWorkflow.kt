package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.safety.clock.Clock

/** Moves near miss reservations through their life cycle and keeps a journal of what it did. */
data class NearMissReservationWorkflow(
    var store: NearMissReservationStore,
    var validator: NearMissReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("near miss reservation $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissReservationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissReservationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("near miss reservation $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newNearMissReservationWorkflow(store: NearMissReservationStore, clock: Clock): NearMissReservationWorkflow {
    return NearMissReservationWorkflow(
        store = store,
        validator = defaultNearMissReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissReservationEngine(),
    )
}
