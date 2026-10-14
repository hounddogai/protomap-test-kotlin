package com.acme.transport.terminal.appointment.window

import com.acme.transport.terminal.clock.Clock

/** Moves appointment rules through their life cycle and keeps a journal of what it did. */
data class AppointmentRuleWorkflow(
    var store: AppointmentRuleStore,
    var validator: AppointmentRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("appointment rule $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentRuleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentRuleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("appointment rule $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAppointmentRuleWorkflow(store: AppointmentRuleStore, clock: Clock): AppointmentRuleWorkflow {
    return AppointmentRuleWorkflow(
        store = store,
        validator = defaultAppointmentRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentRuleEngine(),
    )
}
