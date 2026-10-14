package com.acme.transport.terminal.appointment.entry

import com.acme.transport.terminal.clock.Clock

/** Moves fallback appointment entries through their life cycle and keeps a journal of what it did. */
data class FallbackAppointmentEntryWorkflow(
    var store: FallbackAppointmentEntryStore,
    var validator: FallbackAppointmentEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback appointment entry $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackAppointmentEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback appointment entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackAppointmentEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
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

fun newFallbackAppointmentEntryWorkflow(store: FallbackAppointmentEntryStore, clock: Clock): FallbackAppointmentEntryWorkflow {
    return FallbackAppointmentEntryWorkflow(
        store = store,
        validator = defaultFallbackAppointmentEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
