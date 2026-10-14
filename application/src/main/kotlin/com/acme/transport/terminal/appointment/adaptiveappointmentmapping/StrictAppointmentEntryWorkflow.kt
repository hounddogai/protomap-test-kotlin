package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.terminal.clock.Clock

/** Moves strict appointment entries through their life cycle and keeps a journal of what it did. */
data class StrictAppointmentEntryManager(
    var store: StrictAppointmentEntryStore,
    var validator: StrictAppointmentEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict appointment entry $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAppointmentEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict appointment entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAppointmentEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newStrictAppointmentEntryManager(store: StrictAppointmentEntryStore, clock: Clock): StrictAppointmentEntryManager {
    return StrictAppointmentEntryManager(
        store = store,
        validator = defaultStrictAppointmentEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
