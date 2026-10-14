package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves strict lease return milestones through their life cycle and keeps a journal of what it did.
 */
data class StrictLeaseReturnMilestoneManager(
    var store: StrictLeaseReturnMilestoneStore,
    var validator: StrictLeaseReturnMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictLeaseReturnMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict lease return milestone $code not found")
        validator.requireValid(item)
        item.moveTo(StrictLeaseReturnMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict lease return milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictLeaseReturnMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict lease return milestone $code not found")
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

fun newStrictLeaseReturnMilestoneManager(store: StrictLeaseReturnMilestoneStore, clock: Clock, flags: Registry): StrictLeaseReturnMilestoneManager {
    return StrictLeaseReturnMilestoneManager(
        store = store,
        validator = defaultStrictLeaseReturnMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictLeaseReturnMilestoneEngine(flags),
    )
}
