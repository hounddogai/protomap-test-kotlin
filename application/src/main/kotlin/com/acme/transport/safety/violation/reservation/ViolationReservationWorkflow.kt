package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation reservations through their life cycle and keeps a journal of what it did. */
data class ViolationReservationWorkflow(
    var store: ViolationReservationStore,
    var validator: ViolationReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("violation reservation $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationReservationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("violation reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationReservationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("violation reservation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newViolationReservationWorkflow(store: ViolationReservationStore, clock: Clock, flags: Registry): ViolationReservationWorkflow {
    return ViolationReservationWorkflow(
        store = store,
        validator = defaultViolationReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationReservationEngine(flags),
    )
}
