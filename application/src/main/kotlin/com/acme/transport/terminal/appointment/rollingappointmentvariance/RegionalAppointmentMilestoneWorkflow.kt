package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.terminal.clock.Clock

/**
 * Moves regional appointment milestones through their life cycle and keeps a journal of what it did.
 */
data class RegionalAppointmentMilestoneDesk(
    var store: RegionalAppointmentMilestoneStore,
    var validator: RegionalAppointmentMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional appointment milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalAppointmentMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional appointment milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalAppointmentMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newRegionalAppointmentMilestoneDesk(store: RegionalAppointmentMilestoneStore, clock: Clock): RegionalAppointmentMilestoneDesk {
    return RegionalAppointmentMilestoneDesk(
        store = store,
        validator = defaultRegionalAppointmentMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
