package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.safety.clock.Clock

/** Moves incident rollups through their life cycle and keeps a journal of what it did. */
data class IncidentRollupWorkflow(
    var store: IncidentRollupStore,
    var validator: IncidentRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident rollup $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("incident rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("incident rollup $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newIncidentRollupWorkflow(store: IncidentRollupStore, clock: Clock): IncidentRollupWorkflow {
    return IncidentRollupWorkflow(
        store = store,
        validator = defaultIncidentRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentRollupEngine(),
    )
}
