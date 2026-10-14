package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves tiered sortation summaries through their life cycle and keeps a journal of what it did. */
data class TieredSortationSummaryWorkflow(
    var store: TieredSortationSummaryStore,
    var validator: TieredSortationSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredSortationSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered sortation summary $code not found")
        validator.requireValid(item)
        item.moveTo(TieredSortationSummaryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered sortation summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredSortationSummaryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered sortation summary $code not found")
        val result = engine.evaluate(item)
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

fun newTieredSortationSummaryWorkflow(store: TieredSortationSummaryStore, clock: Clock, flags: Registry): TieredSortationSummaryWorkflow {
    return TieredSortationSummaryWorkflow(
        store = store,
        validator = defaultTieredSortationSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredSortationSummaryEngine(flags),
    )
}
