package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.emissions.clock.Clock

/** Moves route offset plans through their life cycle and keeps a journal of what it did. */
data class RouteOffsetPlanDesk(
    var store: RouteOffsetPlanStore,
    var validator: RouteOffsetPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset plan $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetPlanStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetPlanStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newRouteOffsetPlanDesk(store: RouteOffsetPlanStore, clock: Clock): RouteOffsetPlanDesk {
    return RouteOffsetPlanDesk(
        store = store,
        validator = defaultRouteOffsetPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
