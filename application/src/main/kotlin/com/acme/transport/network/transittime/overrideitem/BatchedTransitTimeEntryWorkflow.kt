package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.network.clock.Clock

/** Moves batched transit time entries through their life cycle and keeps a journal of what it did. */
data class BatchedTransitTimeEntryWorkflow(
    var store: BatchedTransitTimeEntryStore,
    var validator: BatchedTransitTimeEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedTransitTimeEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched transit time entry $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTransitTimeEntryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched transit time entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTransitTimeEntryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched transit time entry $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newBatchedTransitTimeEntryWorkflow(store: BatchedTransitTimeEntryStore, clock: Clock): BatchedTransitTimeEntryWorkflow {
    return BatchedTransitTimeEntryWorkflow(
        store = store,
        validator = defaultBatchedTransitTimeEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedTransitTimeEntryEngine(),
    )
}
