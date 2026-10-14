package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock reservations through their life cycle and keeps a journal of what it did. */
data class CrossDockReservationWorkflow(
    var store: CrossDockReservationStore,
    var validator: CrossDockReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock reservation $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockReservationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockReservationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCrossDockReservationWorkflow(store: CrossDockReservationStore, clock: Clock): CrossDockReservationWorkflow {
    return CrossDockReservationWorkflow(
        store = store,
        validator = defaultCrossDockReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
