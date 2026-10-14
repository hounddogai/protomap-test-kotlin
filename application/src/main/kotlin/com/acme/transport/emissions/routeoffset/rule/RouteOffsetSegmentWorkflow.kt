package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.emissions.clock.Clock

/** Moves route offset segments through their life cycle and keeps a journal of what it did. */
data class RouteOffsetSegmentDesk(
    var store: RouteOffsetSegmentStore,
    var validator: RouteOffsetSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("route offset segment $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetSegmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetSegmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRouteOffsetSegmentDesk(store: RouteOffsetSegmentStore, clock: Clock): RouteOffsetSegmentDesk {
    return RouteOffsetSegmentDesk(
        store = store,
        validator = defaultRouteOffsetSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
