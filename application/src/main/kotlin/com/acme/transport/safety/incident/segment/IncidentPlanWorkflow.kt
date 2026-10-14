package com.acme.transport.safety.incident.segment

import com.acme.transport.safety.clock.Clock

/** Moves incident plans through their life cycle and keeps a journal of what it did. */
data class IncidentPlanDesk(
    var store: IncidentPlanStore,
    var validator: IncidentPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident plan $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("incident plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("incident plan $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newIncidentPlanDesk(store: IncidentPlanStore, clock: Clock): IncidentPlanDesk {
    return IncidentPlanDesk(
        store = store,
        validator = defaultIncidentPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentPlanEngine(),
    )
}
