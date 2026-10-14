package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.terminal.clock.Clock

/** Moves legacy appointment bundles through their life cycle and keeps a journal of what it did. */
data class LegacyAppointmentBundleWorkflow(
    var store: LegacyAppointmentBundleStore,
    var validator: LegacyAppointmentBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyAppointmentBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy appointment bundle $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAppointmentBundleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy appointment bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAppointmentBundleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy appointment bundle $code not found")
        val result = engine.evaluate(item)
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

fun newLegacyAppointmentBundleWorkflow(store: LegacyAppointmentBundleStore, clock: Clock): LegacyAppointmentBundleWorkflow {
    return LegacyAppointmentBundleWorkflow(
        store = store,
        validator = defaultLegacyAppointmentBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyAppointmentBundleEngine(),
    )
}
