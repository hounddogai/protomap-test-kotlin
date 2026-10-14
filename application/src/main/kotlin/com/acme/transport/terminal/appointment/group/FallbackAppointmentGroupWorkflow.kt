package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves fallback appointment groups through their life cycle and keeps a journal of what it did. */
data class FallbackAppointmentGroupCoordinator(
    var store: FallbackAppointmentGroupStore,
    var validator: FallbackAppointmentGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackAppointmentGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback appointment group $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackAppointmentGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback appointment group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackAppointmentGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback appointment group $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFallbackAppointmentGroupCoordinator(store: FallbackAppointmentGroupStore, clock: Clock, flags: Registry): FallbackAppointmentGroupCoordinator {
    return FallbackAppointmentGroupCoordinator(
        store = store,
        validator = defaultFallbackAppointmentGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackAppointmentGroupEngine(flags),
    )
}
