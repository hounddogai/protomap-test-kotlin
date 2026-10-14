package com.acme.transport.terminal.appointment.segment

import com.acme.transport.terminal.clock.Clock

/** Moves appointment segments through their life cycle and keeps a journal of what it did. */
data class AppointmentSegmentService(
    var store: AppointmentSegmentStore,
    var validator: AppointmentSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("appointment segment $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentSegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentSegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newAppointmentSegmentService(store: AppointmentSegmentStore, clock: Clock): AppointmentSegmentService {
    return AppointmentSegmentService(
        store = store,
        validator = defaultAppointmentSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
