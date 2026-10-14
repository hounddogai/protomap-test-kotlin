package com.acme.transport.network.transittime.budget

import com.acme.transport.network.clock.Clock

/** Moves transit time summaries through their life cycle and keeps a journal of what it did. */
data class TransitTimeSummaryCoordinator(
    var store: TransitTimeSummaryStore,
    var validator: TransitTimeSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("transit time summary $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeSummaryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeSummaryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newTransitTimeSummaryCoordinator(store: TransitTimeSummaryStore, clock: Clock): TransitTimeSummaryCoordinator {
    return TransitTimeSummaryCoordinator(
        store = store,
        validator = defaultTransitTimeSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
