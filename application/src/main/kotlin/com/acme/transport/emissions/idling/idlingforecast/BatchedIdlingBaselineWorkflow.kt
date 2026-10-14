package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.emissions.clock.Clock

/** Moves batched idling baselines through their life cycle and keeps a journal of what it did. */
data class BatchedIdlingBaselineManager(
    var store: BatchedIdlingBaselineStore,
    var validator: BatchedIdlingBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched idling baseline $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedIdlingBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched idling baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedIdlingBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newBatchedIdlingBaselineManager(store: BatchedIdlingBaselineStore, clock: Clock): BatchedIdlingBaselineManager {
    return BatchedIdlingBaselineManager(
        store = store,
        validator = defaultBatchedIdlingBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
