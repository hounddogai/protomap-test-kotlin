package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.emissions.clock.Clock

/** Moves route offset claims through their life cycle and keeps a journal of what it did. */
data class RouteOffsetClaimCoordinator(
    var store: RouteOffsetClaimStore,
    var validator: RouteOffsetClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("route offset claim $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("route offset claim $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newRouteOffsetClaimCoordinator(store: RouteOffsetClaimStore, clock: Clock): RouteOffsetClaimCoordinator {
    return RouteOffsetClaimCoordinator(
        store = store,
        validator = defaultRouteOffsetClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetClaimEngine(),
    )
}
