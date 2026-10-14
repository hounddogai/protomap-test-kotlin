package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.terminal.clock.Clock

/** Moves appointment mappings through their life cycle and keeps a journal of what it did. */
data class AppointmentMappingManager(
    var store: AppointmentMappingStore,
    var validator: AppointmentMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("appointment mapping $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentMappingStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentMappingStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("appointment mapping $code not found")
        val result = engine.price(item)
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

fun newAppointmentMappingManager(store: AppointmentMappingStore, clock: Clock): AppointmentMappingManager {
    return AppointmentMappingManager(
        store = store,
        validator = defaultAppointmentMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentMappingEngine(),
    )
}
