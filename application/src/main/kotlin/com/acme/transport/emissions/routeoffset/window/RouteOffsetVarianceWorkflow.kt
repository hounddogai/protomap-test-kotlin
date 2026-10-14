package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.clock.Clock

/** Moves route offset variances through their life cycle and keeps a journal of what it did. */
data class RouteOffsetVarianceWorkflow(
    var store: RouteOffsetVarianceStore,
    var validator: RouteOffsetVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset variance $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetVarianceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetVarianceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newRouteOffsetVarianceWorkflow(store: RouteOffsetVarianceStore, clock: Clock): RouteOffsetVarianceWorkflow {
    return RouteOffsetVarianceWorkflow(
        store = store,
        validator = defaultRouteOffsetVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
