package com.acme.transport.safety.incident.digest

import com.acme.transport.safety.clock.Clock

/** Moves incident rules through their life cycle and keeps a journal of what it did. */
data class IncidentRuleCoordinator(
    var store: IncidentRuleStore,
    var validator: IncidentRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident rule $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("incident rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("incident rule $code not found")
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

fun newIncidentRuleCoordinator(store: IncidentRuleStore, clock: Clock): IncidentRuleCoordinator {
    return IncidentRuleCoordinator(
        store = store,
        validator = defaultIncidentRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentRuleEngine(),
    )
}
