package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.terminal.clock.Clock

/** Moves lenient appointment mappings through their life cycle and keeps a journal of what it did. */
data class LenientAppointmentMappingCoordinator(
    var store: LenientAppointmentMappingStore,
    var validator: LenientAppointmentMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientAppointmentMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient appointment mapping $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAppointmentMappingStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient appointment mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAppointmentMappingStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient appointment mapping $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newLenientAppointmentMappingCoordinator(store: LenientAppointmentMappingStore, clock: Clock): LenientAppointmentMappingCoordinator {
    return LenientAppointmentMappingCoordinator(
        store = store,
        validator = defaultLenientAppointmentMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientAppointmentMappingEngine(),
    )
}
