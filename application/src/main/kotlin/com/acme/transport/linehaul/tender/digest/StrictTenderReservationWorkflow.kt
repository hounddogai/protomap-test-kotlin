package com.acme.transport.linehaul.tender.digest

import com.acme.transport.linehaul.clock.Clock

/** Moves strict tender reservations through their life cycle and keeps a journal of what it did. */
data class StrictTenderReservationWorkflow(
    var store: StrictTenderReservationStore,
    var validator: StrictTenderReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict tender reservation $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTenderReservationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict tender reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTenderReservationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newStrictTenderReservationWorkflow(store: StrictTenderReservationStore, clock: Clock): StrictTenderReservationWorkflow {
    return StrictTenderReservationWorkflow(
        store = store,
        validator = defaultStrictTenderReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
