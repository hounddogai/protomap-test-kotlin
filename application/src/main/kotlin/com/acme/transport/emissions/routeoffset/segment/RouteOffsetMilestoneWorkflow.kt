package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.emissions.clock.Clock

/** Moves route offset milestones through their life cycle and keeps a journal of what it did. */
data class RouteOffsetMilestoneWorkflow(
    var store: RouteOffsetMilestoneStore,
    var validator: RouteOffsetMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("route offset milestone $code not found")
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

fun newRouteOffsetMilestoneWorkflow(store: RouteOffsetMilestoneStore, clock: Clock): RouteOffsetMilestoneWorkflow {
    return RouteOffsetMilestoneWorkflow(
        store = store,
        validator = defaultRouteOffsetMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetMilestoneEngine(),
    )
}
