package com.acme.transport.linehaul.tender.slot

import com.acme.transport.linehaul.clock.Clock

/** Moves tender reservations through their life cycle and keeps a journal of what it did. */
data class TenderReservationWorkflow(
    var store: TenderReservationStore,
    var validator: TenderReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tender reservation $code not found")
        validator.requireValid(item)
        item.moveTo(TenderReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tender reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTenderReservationWorkflow(store: TenderReservationStore, clock: Clock): TenderReservationWorkflow {
    return TenderReservationWorkflow(
        store = store,
        validator = defaultTenderReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
