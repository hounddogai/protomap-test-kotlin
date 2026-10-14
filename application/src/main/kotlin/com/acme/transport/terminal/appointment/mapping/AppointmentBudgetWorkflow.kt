package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.terminal.clock.Clock

/** Moves appointment budgets through their life cycle and keeps a journal of what it did. */
data class AppointmentBudgetDesk(
    var store: AppointmentBudgetStore,
    var validator: AppointmentBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("appointment budget $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("appointment budget $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newAppointmentBudgetDesk(store: AppointmentBudgetStore, clock: Clock): AppointmentBudgetDesk {
    return AppointmentBudgetDesk(
        store = store,
        validator = defaultAppointmentBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentBudgetEngine(),
    )
}
