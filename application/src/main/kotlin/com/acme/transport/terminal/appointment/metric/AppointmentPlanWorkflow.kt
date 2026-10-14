package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves appointment plans through their life cycle and keeps a journal of what it did. */
data class AppointmentPlanDesk(
    var store: AppointmentPlanStore,
    var validator: AppointmentPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("appointment plan $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentPlanStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentPlanStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("appointment plan $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newAppointmentPlanDesk(store: AppointmentPlanStore, clock: Clock, flags: Registry): AppointmentPlanDesk {
    return AppointmentPlanDesk(
        store = store,
        validator = defaultAppointmentPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentPlanEngine(flags),
    )
}
