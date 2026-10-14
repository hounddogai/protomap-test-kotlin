package com.acme.transport.terminal.appointment.policy

import com.acme.transport.terminal.clock.Clock

/** Moves appointment slots through their life cycle and keeps a journal of what it did. */
data class AppointmentSlotWorkflow(
    var store: AppointmentSlotStore,
    var validator: AppointmentSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("appointment slot $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("appointment slot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newAppointmentSlotWorkflow(store: AppointmentSlotStore, clock: Clock): AppointmentSlotWorkflow {
    return AppointmentSlotWorkflow(
        store = store,
        validator = defaultAppointmentSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentSlotEngine(),
    )
}
