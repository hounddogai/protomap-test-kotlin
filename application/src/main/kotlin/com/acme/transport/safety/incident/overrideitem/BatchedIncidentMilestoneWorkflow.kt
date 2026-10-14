package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves batched incident milestones through their life cycle and keeps a journal of what it did. */
data class BatchedIncidentMilestoneDesk(
    var store: BatchedIncidentMilestoneStore,
    var validator: BatchedIncidentMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedIncidentMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched incident milestone $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedIncidentMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched incident milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedIncidentMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched incident milestone $code not found")
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

fun newBatchedIncidentMilestoneDesk(store: BatchedIncidentMilestoneStore, clock: Clock, flags: Registry): BatchedIncidentMilestoneDesk {
    return BatchedIncidentMilestoneDesk(
        store = store,
        validator = defaultBatchedIncidentMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedIncidentMilestoneEngine(flags),
    )
}
