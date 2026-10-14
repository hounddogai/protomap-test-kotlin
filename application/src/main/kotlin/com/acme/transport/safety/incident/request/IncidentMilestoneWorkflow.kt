package com.acme.transport.safety.incident.request

import com.acme.transport.safety.clock.Clock

/** Moves incident milestones through their life cycle and keeps a journal of what it did. */
data class IncidentMilestoneCoordinator(
    var store: IncidentMilestoneStore,
    var validator: IncidentMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("incident milestone $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("incident milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("incident milestone $code not found")
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

fun newIncidentMilestoneCoordinator(store: IncidentMilestoneStore, clock: Clock): IncidentMilestoneCoordinator {
    return IncidentMilestoneCoordinator(
        store = store,
        validator = defaultIncidentMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentMilestoneEngine(),
    )
}
