package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.safety.clock.Clock

/** Moves incident slots through their life cycle and keeps a journal of what it did. */
data class IncidentSlotWorkflow(
    var store: IncidentSlotStore,
    var validator: IncidentSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("incident slot $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("incident slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newIncidentSlotWorkflow(store: IncidentSlotStore, clock: Clock): IncidentSlotWorkflow {
    return IncidentSlotWorkflow(
        store = store,
        validator = defaultIncidentSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
