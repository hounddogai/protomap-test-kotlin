package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.clock.Clock

/** Moves route offset records through their life cycle and keeps a journal of what it did. */
data class RouteOffsetRecordService(
    var store: RouteOffsetRecordStore,
    var validator: RouteOffsetRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("route offset record $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newRouteOffsetRecordService(store: RouteOffsetRecordStore, clock: Clock): RouteOffsetRecordService {
    return RouteOffsetRecordService(
        store = store,
        validator = defaultRouteOffsetRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
