package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.terminal.clock.Clock

/** Moves appointment baselines through their life cycle and keeps a journal of what it did. */
data class AppointmentBaselineWorkflow(
    var store: AppointmentBaselineStore,
    var validator: AppointmentBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("appointment baseline $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newAppointmentBaselineWorkflow(store: AppointmentBaselineStore, clock: Clock): AppointmentBaselineWorkflow {
    return AppointmentBaselineWorkflow(
        store = store,
        validator = defaultAppointmentBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
