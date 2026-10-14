package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.clock.Clock

/** Moves route offset summaries through their life cycle and keeps a journal of what it did. */
data class RouteOffsetSummaryService(
    var store: RouteOffsetSummaryStore,
    var validator: RouteOffsetSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("route offset summary $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetSummaryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetSummaryStatus.WITHDRAWN)
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

fun newRouteOffsetSummaryService(store: RouteOffsetSummaryStore, clock: Clock): RouteOffsetSummaryService {
    return RouteOffsetSummaryService(
        store = store,
        validator = defaultRouteOffsetSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
