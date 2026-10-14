package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.terminal.clock.Clock

/**
 * Moves scheduled cross dock summaries through their life cycle and keeps a journal of what it did.
 */
data class ScheduledCrossDockSummaryManager(
    var store: ScheduledCrossDockSummaryStore,
    var validator: ScheduledCrossDockSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled cross dock summary $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCrossDockSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled cross dock summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCrossDockSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newScheduledCrossDockSummaryManager(store: ScheduledCrossDockSummaryStore, clock: Clock): ScheduledCrossDockSummaryManager {
    return ScheduledCrossDockSummaryManager(
        store = store,
        validator = defaultScheduledCrossDockSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
