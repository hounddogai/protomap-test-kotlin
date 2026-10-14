package com.acme.transport.network.node.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves scheduled node milestones through their life cycle and keeps a journal of what it did. */
data class ScheduledNodeMilestoneManager(
    var store: ScheduledNodeMilestoneStore,
    var validator: ScheduledNodeMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledNodeMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled node milestone $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledNodeMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled node milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledNodeMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled node milestone $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newScheduledNodeMilestoneManager(store: ScheduledNodeMilestoneStore, clock: Clock, flags: Registry): ScheduledNodeMilestoneManager {
    return ScheduledNodeMilestoneManager(
        store = store,
        validator = defaultScheduledNodeMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledNodeMilestoneEngine(flags),
    )
}
