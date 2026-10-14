package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.emissions.clock.Clock

/** Moves route offset requests through their life cycle and keeps a journal of what it did. */
data class RouteOffsetRequestDesk(
    var store: RouteOffsetRequestStore,
    var validator: RouteOffsetRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset request $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetRequestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetRequestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newRouteOffsetRequestDesk(store: RouteOffsetRequestStore, clock: Clock): RouteOffsetRequestDesk {
    return RouteOffsetRequestDesk(
        store = store,
        validator = defaultRouteOffsetRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
