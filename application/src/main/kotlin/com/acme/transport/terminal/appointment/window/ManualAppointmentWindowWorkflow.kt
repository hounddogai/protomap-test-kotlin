package com.acme.transport.terminal.appointment.window

import com.acme.transport.terminal.clock.Clock

/** Moves manual appointment windows through their life cycle and keeps a journal of what it did. */
data class ManualAppointmentWindowService(
    var store: ManualAppointmentWindowStore,
    var validator: ManualAppointmentWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualAppointmentWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual appointment window $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAppointmentWindowStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual appointment window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAppointmentWindowStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual appointment window $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newManualAppointmentWindowService(store: ManualAppointmentWindowStore, clock: Clock): ManualAppointmentWindowService {
    return ManualAppointmentWindowService(
        store = store,
        validator = defaultManualAppointmentWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualAppointmentWindowEngine(),
    )
}
