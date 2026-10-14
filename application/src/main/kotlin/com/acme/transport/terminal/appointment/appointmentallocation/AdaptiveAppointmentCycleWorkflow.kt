package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves adaptive appointment cycles through their life cycle and keeps a journal of what it did. */
data class AdaptiveAppointmentCycleDesk(
    var store: AdaptiveAppointmentCycleStore,
    var validator: AdaptiveAppointmentCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveAppointmentCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive appointment cycle $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAppointmentCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive appointment cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAppointmentCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive appointment cycle $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newAdaptiveAppointmentCycleDesk(store: AdaptiveAppointmentCycleStore, clock: Clock, flags: Registry): AdaptiveAppointmentCycleDesk {
    return AdaptiveAppointmentCycleDesk(
        store = store,
        validator = defaultAdaptiveAppointmentCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveAppointmentCycleEngine(flags),
    )
}
