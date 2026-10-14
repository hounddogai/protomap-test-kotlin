package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves rolling incident plans through their life cycle and keeps a journal of what it did. */
data class RollingIncidentPlanWorkflow(
    var store: RollingIncidentPlanStore,
    var validator: RollingIncidentPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingIncidentPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling incident plan $code not found")
        validator.requireValid(item)
        item.moveTo(RollingIncidentPlanStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling incident plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingIncidentPlanStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling incident plan $code not found")
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

fun newRollingIncidentPlanWorkflow(store: RollingIncidentPlanStore, clock: Clock, flags: Registry): RollingIncidentPlanWorkflow {
    return RollingIncidentPlanWorkflow(
        store = store,
        validator = defaultRollingIncidentPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingIncidentPlanEngine(flags),
    )
}
