package com.acme.transport.safety.incident.proposal

import com.acme.transport.safety.clock.Clock

/** Moves incident tiers through their life cycle and keeps a journal of what it did. */
data class IncidentTierWorkflow(
    var store: IncidentTierStore,
    var validator: IncidentTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("incident tier $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentTierStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("incident tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentTierStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("incident tier $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newIncidentTierWorkflow(store: IncidentTierStore, clock: Clock): IncidentTierWorkflow {
    return IncidentTierWorkflow(
        store = store,
        validator = defaultIncidentTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentTierEngine(),
    )
}
