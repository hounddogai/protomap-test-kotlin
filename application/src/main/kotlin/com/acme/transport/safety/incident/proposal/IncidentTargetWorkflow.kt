package com.acme.transport.safety.incident.proposal

import com.acme.transport.safety.clock.Clock

/** Moves incident targets through their life cycle and keeps a journal of what it did. */
data class IncidentTargetCoordinator(
    var store: IncidentTargetStore,
    var validator: IncidentTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("incident target $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("incident target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentTargetStatus.CANCELLED)
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

fun newIncidentTargetCoordinator(store: IncidentTargetStore, clock: Clock): IncidentTargetCoordinator {
    return IncidentTargetCoordinator(
        store = store,
        validator = defaultIncidentTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
