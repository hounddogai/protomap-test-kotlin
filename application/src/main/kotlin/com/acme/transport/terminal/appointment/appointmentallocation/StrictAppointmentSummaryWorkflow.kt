package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves strict appointment summaries through their life cycle and keeps a journal of what it did. */
data class StrictAppointmentSummaryDesk(
    var store: StrictAppointmentSummaryStore,
    var validator: StrictAppointmentSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictAppointmentSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict appointment summary $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAppointmentSummaryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict appointment summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAppointmentSummaryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict appointment summary $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newStrictAppointmentSummaryDesk(store: StrictAppointmentSummaryStore, clock: Clock, flags: Registry): StrictAppointmentSummaryDesk {
    return StrictAppointmentSummaryDesk(
        store = store,
        validator = defaultStrictAppointmentSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictAppointmentSummaryEngine(flags),
    )
}
