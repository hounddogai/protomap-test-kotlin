package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves global redelivery milestones through their life cycle and keeps a journal of what it did. */
data class GlobalRedeliveryMilestoneWorkflow(
    var store: GlobalRedeliveryMilestoneStore,
    var validator: GlobalRedeliveryMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRedeliveryMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global redelivery milestone $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRedeliveryMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global redelivery milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRedeliveryMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global redelivery milestone $code not found")
        val result = engine.compute(item)
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

fun newGlobalRedeliveryMilestoneWorkflow(store: GlobalRedeliveryMilestoneStore, clock: Clock, flags: Registry): GlobalRedeliveryMilestoneWorkflow {
    return GlobalRedeliveryMilestoneWorkflow(
        store = store,
        validator = defaultGlobalRedeliveryMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRedeliveryMilestoneEngine(flags),
    )
}
