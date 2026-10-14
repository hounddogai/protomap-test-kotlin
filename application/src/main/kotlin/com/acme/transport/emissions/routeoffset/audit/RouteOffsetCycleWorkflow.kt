package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.clock.Clock

/** Moves route offset cycles through their life cycle and keeps a journal of what it did. */
data class RouteOffsetCycleDesk(
    var store: RouteOffsetCycleStore,
    var validator: RouteOffsetCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset cycle $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetCycleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetCycleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newRouteOffsetCycleDesk(store: RouteOffsetCycleStore, clock: Clock): RouteOffsetCycleDesk {
    return RouteOffsetCycleDesk(
        store = store,
        validator = defaultRouteOffsetCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
