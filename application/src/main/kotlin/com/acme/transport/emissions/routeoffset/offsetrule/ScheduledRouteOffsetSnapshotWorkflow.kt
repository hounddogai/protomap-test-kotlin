package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.emissions.clock.Clock

/**
 * Moves scheduled route offset snapshots through their life cycle and keeps a journal of what it did.
 */
data class ScheduledRouteOffsetSnapshotManager(
    var store: ScheduledRouteOffsetSnapshotStore,
    var validator: ScheduledRouteOffsetSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled route offset snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRouteOffsetSnapshotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled route offset snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRouteOffsetSnapshotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newScheduledRouteOffsetSnapshotManager(store: ScheduledRouteOffsetSnapshotStore, clock: Clock): ScheduledRouteOffsetSnapshotManager {
    return ScheduledRouteOffsetSnapshotManager(
        store = store,
        validator = defaultScheduledRouteOffsetSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
