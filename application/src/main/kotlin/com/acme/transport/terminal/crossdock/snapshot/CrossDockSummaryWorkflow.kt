package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock summaries through their life cycle and keeps a journal of what it did. */
data class CrossDockSummaryDesk(
    var store: CrossDockSummaryStore,
    var validator: CrossDockSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock summary $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockSummaryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockSummaryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newCrossDockSummaryDesk(store: CrossDockSummaryStore, clock: Clock): CrossDockSummaryDesk {
    return CrossDockSummaryDesk(
        store = store,
        validator = defaultCrossDockSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
