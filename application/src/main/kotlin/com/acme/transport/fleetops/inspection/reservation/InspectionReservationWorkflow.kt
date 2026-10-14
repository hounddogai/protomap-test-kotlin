package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection reservations through their life cycle and keeps a journal of what it did. */
data class InspectionReservationDesk(
    var store: InspectionReservationStore,
    var validator: InspectionReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection reservation $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionReservationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionReservationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newInspectionReservationDesk(store: InspectionReservationStore, clock: Clock): InspectionReservationDesk {
    return InspectionReservationDesk(
        store = store,
        validator = defaultInspectionReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
