package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.terminal.clock.Clock

/**
 * Moves scheduled appointment forecasts through their life cycle and keeps a journal of what it did.
 */
data class ScheduledAppointmentForecastWorkflow(
    var store: ScheduledAppointmentForecastStore,
    var validator: ScheduledAppointmentForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled appointment forecast $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAppointmentForecastStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled appointment forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAppointmentForecastStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newScheduledAppointmentForecastWorkflow(store: ScheduledAppointmentForecastStore, clock: Clock): ScheduledAppointmentForecastWorkflow {
    return ScheduledAppointmentForecastWorkflow(
        store = store,
        validator = defaultScheduledAppointmentForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
