package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.clock.Clock

/** Moves route offset profiles through their life cycle and keeps a journal of what it did. */
data class RouteOffsetProfileCoordinator(
    var store: RouteOffsetProfileStore,
    var validator: RouteOffsetProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("route offset profile $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("route offset profile $code not found")
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

fun newRouteOffsetProfileCoordinator(store: RouteOffsetProfileStore, clock: Clock): RouteOffsetProfileCoordinator {
    return RouteOffsetProfileCoordinator(
        store = store,
        validator = defaultRouteOffsetProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetProfileEngine(),
    )
}
