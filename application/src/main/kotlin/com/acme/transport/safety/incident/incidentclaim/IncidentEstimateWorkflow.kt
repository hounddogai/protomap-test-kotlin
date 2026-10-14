package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.safety.clock.Clock

/** Moves incident estimates through their life cycle and keeps a journal of what it did. */
data class IncidentEstimateCoordinator(
    var store: IncidentEstimateStore,
    var validator: IncidentEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident estimate $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentEstimateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("incident estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentEstimateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newIncidentEstimateCoordinator(store: IncidentEstimateStore, clock: Clock): IncidentEstimateCoordinator {
    return IncidentEstimateCoordinator(
        store = store,
        validator = defaultIncidentEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
