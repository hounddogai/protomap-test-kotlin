package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves appointment windows through their life cycle and keeps a journal of what it did. */
data class AppointmentWindowCoordinator(
    var store: AppointmentWindowStore,
    var validator: AppointmentWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("appointment window $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentWindowStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentWindowStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("appointment window $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newAppointmentWindowCoordinator(store: AppointmentWindowStore, clock: Clock, flags: Registry): AppointmentWindowCoordinator {
    return AppointmentWindowCoordinator(
        store = store,
        validator = defaultAppointmentWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentWindowEngine(flags),
    )
}
