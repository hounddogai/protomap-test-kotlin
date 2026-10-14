package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves utilization milestones through their life cycle and keeps a journal of what it did. */
data class UtilizationMilestoneWorkflow(
    var store: UtilizationMilestoneStore,
    var validator: UtilizationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("utilization milestone $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("utilization milestone $code not found")
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

fun newUtilizationMilestoneWorkflow(store: UtilizationMilestoneStore, clock: Clock, flags: Registry): UtilizationMilestoneWorkflow {
    return UtilizationMilestoneWorkflow(
        store = store,
        validator = defaultUtilizationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationMilestoneEngine(flags),
    )
}
