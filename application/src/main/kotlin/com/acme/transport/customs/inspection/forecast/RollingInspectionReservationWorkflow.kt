package com.acme.transport.customs.inspection.forecast

import com.acme.transport.customs.clock.Clock

/**
 * Moves rolling inspection reservations through their life cycle and keeps a journal of what it did.
 */
data class RollingInspectionReservationDesk(
    var store: RollingInspectionReservationStore,
    var validator: RollingInspectionReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling inspection reservation $code not found")
        validator.requireValid(item)
        item.moveTo(RollingInspectionReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling inspection reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingInspectionReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newRollingInspectionReservationDesk(store: RollingInspectionReservationStore, clock: Clock): RollingInspectionReservationDesk {
    return RollingInspectionReservationDesk(
        store = store,
        validator = defaultRollingInspectionReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
