package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.emissions.clock.Clock

/** Moves route offset policies through their life cycle and keeps a journal of what it did. */
data class RouteOffsetPolicyDesk(
    var store: RouteOffsetPolicyStore,
    var validator: RouteOffsetPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("route offset policy $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("route offset policy $code not found")
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

fun newRouteOffsetPolicyDesk(store: RouteOffsetPolicyStore, clock: Clock): RouteOffsetPolicyDesk {
    return RouteOffsetPolicyDesk(
        store = store,
        validator = defaultRouteOffsetPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetPolicyEngine(),
    )
}
