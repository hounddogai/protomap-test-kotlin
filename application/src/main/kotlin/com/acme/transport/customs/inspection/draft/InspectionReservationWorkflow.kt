package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clock.Clock

/** Moves inspection reservations through their life cycle and keeps a journal of what it did. */
data class InspectionReservationWorkflow(
    var store: InspectionReservationStore,
    var validator: InspectionReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection reservation $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionReservationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionReservationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection reservation $code not found")
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

fun newInspectionReservationWorkflow(store: InspectionReservationStore, clock: Clock): InspectionReservationWorkflow {
    return InspectionReservationWorkflow(
        store = store,
        validator = defaultInspectionReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionReservationEngine(),
    )
}
