package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.emissions.clock.Clock

/**
 * Moves scheduled route offset allocations through their life cycle and keeps a journal of what it did.
 */
data class ScheduledRouteOffsetAllocationManager(
    var store: ScheduledRouteOffsetAllocationStore,
    var validator: ScheduledRouteOffsetAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRouteOffsetAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled route offset allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRouteOffsetAllocationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled route offset allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRouteOffsetAllocationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled route offset allocation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newScheduledRouteOffsetAllocationManager(store: ScheduledRouteOffsetAllocationStore, clock: Clock): ScheduledRouteOffsetAllocationManager {
    return ScheduledRouteOffsetAllocationManager(
        store = store,
        validator = defaultScheduledRouteOffsetAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRouteOffsetAllocationEngine(),
    )
}
