package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.network.clock.Clock

/** Moves hub reservations through their life cycle and keeps a journal of what it did. */
data class HubReservationWorkflow(
    var store: HubReservationStore,
    var validator: HubReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("hub reservation $code not found")
        validator.requireValid(item)
        item.moveTo(HubReservationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubReservationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newHubReservationWorkflow(store: HubReservationStore, clock: Clock): HubReservationWorkflow {
    return HubReservationWorkflow(
        store = store,
        validator = defaultHubReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
