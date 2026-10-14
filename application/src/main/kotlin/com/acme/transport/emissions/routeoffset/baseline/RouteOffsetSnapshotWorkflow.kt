package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.clock.Clock

/** Moves route offset snapshots through their life cycle and keeps a journal of what it did. */
data class RouteOffsetSnapshotDesk(
    var store: RouteOffsetSnapshotStore,
    var validator: RouteOffsetSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("route offset snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetSnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetSnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("route offset snapshot $code not found")
        val result = engine.price(item)
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

fun newRouteOffsetSnapshotDesk(store: RouteOffsetSnapshotStore, clock: Clock): RouteOffsetSnapshotDesk {
    return RouteOffsetSnapshotDesk(
        store = store,
        validator = defaultRouteOffsetSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetSnapshotEngine(),
    )
}
