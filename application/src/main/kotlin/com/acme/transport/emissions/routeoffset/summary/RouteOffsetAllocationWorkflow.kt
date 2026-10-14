package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.clock.Clock

/** Moves route offset allocations through their life cycle and keeps a journal of what it did. */
data class RouteOffsetAllocationCoordinator(
    var store: RouteOffsetAllocationStore,
    var validator: RouteOffsetAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset allocation $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetAllocationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetAllocationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("route offset allocation $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newRouteOffsetAllocationCoordinator(store: RouteOffsetAllocationStore, clock: Clock): RouteOffsetAllocationCoordinator {
    return RouteOffsetAllocationCoordinator(
        store = store,
        validator = defaultRouteOffsetAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetAllocationEngine(),
    )
}
