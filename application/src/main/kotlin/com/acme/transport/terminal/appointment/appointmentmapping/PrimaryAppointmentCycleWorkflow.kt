package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.terminal.clock.Clock

/** Moves primary appointment cycles through their life cycle and keeps a journal of what it did. */
data class PrimaryAppointmentCycleService(
    var store: PrimaryAppointmentCycleStore,
    var validator: PrimaryAppointmentCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryAppointmentCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary appointment cycle $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAppointmentCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary appointment cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAppointmentCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary appointment cycle $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newPrimaryAppointmentCycleService(store: PrimaryAppointmentCycleStore, clock: Clock): PrimaryAppointmentCycleService {
    return PrimaryAppointmentCycleService(
        store = store,
        validator = defaultPrimaryAppointmentCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryAppointmentCycleEngine(),
    )
}
