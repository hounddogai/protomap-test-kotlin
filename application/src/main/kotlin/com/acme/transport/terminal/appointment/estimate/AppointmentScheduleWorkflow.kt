package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.terminal.clock.Clock

/** Moves appointment schedules through their life cycle and keeps a journal of what it did. */
data class AppointmentScheduleCoordinator(
    var store: AppointmentScheduleStore,
    var validator: AppointmentScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("appointment schedule $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newAppointmentScheduleCoordinator(store: AppointmentScheduleStore, clock: Clock): AppointmentScheduleCoordinator {
    return AppointmentScheduleCoordinator(
        store = store,
        validator = defaultAppointmentScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
