package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.terminal.clock.Clock

/** Moves legacy appointment segments through their life cycle and keeps a journal of what it did. */
data class LegacyAppointmentSegmentCoordinator(
    var store: LegacyAppointmentSegmentStore,
    var validator: LegacyAppointmentSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy appointment segment $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAppointmentSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy appointment segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAppointmentSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLegacyAppointmentSegmentCoordinator(store: LegacyAppointmentSegmentStore, clock: Clock): LegacyAppointmentSegmentCoordinator {
    return LegacyAppointmentSegmentCoordinator(
        store = store,
        validator = defaultLegacyAppointmentSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
