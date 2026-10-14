package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break milestones through their life cycle and keeps a journal of what it did. */
data class RestBreakMilestoneService(
    var store: RestBreakMilestoneStore,
    var validator: RestBreakMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rest break milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rest break milestone $code not found")
        val result = engine.price(item)
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

fun newRestBreakMilestoneService(store: RestBreakMilestoneStore, clock: Clock, flags: Registry): RestBreakMilestoneService {
    return RestBreakMilestoneService(
        store = store,
        validator = defaultRestBreakMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakMilestoneEngine(flags),
    )
}
