package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel reservations through their life cycle and keeps a journal of what it did. */
data class ParcelReservationWorkflow(
    var store: ParcelReservationStore,
    var validator: ParcelReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("parcel reservation $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelReservationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelReservationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newParcelReservationWorkflow(store: ParcelReservationStore, clock: Clock): ParcelReservationWorkflow {
    return ParcelReservationWorkflow(
        store = store,
        validator = defaultParcelReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
