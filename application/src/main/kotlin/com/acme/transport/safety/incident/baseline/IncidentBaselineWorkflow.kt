package com.acme.transport.safety.incident.baseline

import com.acme.transport.safety.clock.Clock

/** Moves incident baselines through their life cycle and keeps a journal of what it did. */
data class IncidentBaselineService(
    var store: IncidentBaselineStore,
    var validator: IncidentBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident baseline $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentBaselineStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("incident baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentBaselineStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newIncidentBaselineService(store: IncidentBaselineStore, clock: Clock): IncidentBaselineService {
    return IncidentBaselineService(
        store = store,
        validator = defaultIncidentBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
