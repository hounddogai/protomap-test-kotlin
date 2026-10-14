package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.linehaul.clock.Clock

/** Moves pending backhaul reservations through their life cycle and keeps a journal of what it did. */
data class PendingBackhaulReservationManager(
    var store: PendingBackhaulReservationStore,
    var validator: PendingBackhaulReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending backhaul reservation $code not found")
        validator.requireValid(item)
        item.moveTo(PendingBackhaulReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending backhaul reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingBackhaulReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newPendingBackhaulReservationManager(store: PendingBackhaulReservationStore, clock: Clock): PendingBackhaulReservationManager {
    return PendingBackhaulReservationManager(
        store = store,
        validator = defaultPendingBackhaulReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
