package com.acme.transport.terminal.appointment.variance

import com.acme.transport.terminal.clock.Clock

/** Moves appointment milestones through their life cycle and keeps a journal of what it did. */
data class AppointmentMilestoneDesk(
    var store: AppointmentMilestoneStore,
    var validator: AppointmentMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("appointment milestone $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newAppointmentMilestoneDesk(store: AppointmentMilestoneStore, clock: Clock): AppointmentMilestoneDesk {
    return AppointmentMilestoneDesk(
        store = store,
        validator = defaultAppointmentMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
