package com.acme.transport.terminal.appointment.group

import com.acme.transport.terminal.clock.Clock

/** Moves appointment assignments through their life cycle and keeps a journal of what it did. */
data class AppointmentAssignmentCoordinator(
    var store: AppointmentAssignmentStore,
    var validator: AppointmentAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("appointment assignment $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newAppointmentAssignmentCoordinator(store: AppointmentAssignmentStore, clock: Clock): AppointmentAssignmentCoordinator {
    return AppointmentAssignmentCoordinator(
        store = store,
        validator = defaultAppointmentAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
