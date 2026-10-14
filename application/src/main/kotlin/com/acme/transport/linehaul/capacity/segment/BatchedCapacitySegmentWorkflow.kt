package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.linehaul.clock.Clock

/** Moves batched capacity segments through their life cycle and keeps a journal of what it did. */
data class BatchedCapacitySegmentManager(
    var store: BatchedCapacitySegmentStore,
    var validator: BatchedCapacitySegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched capacity segment $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedCapacitySegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched capacity segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedCapacitySegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newBatchedCapacitySegmentManager(store: BatchedCapacitySegmentStore, clock: Clock): BatchedCapacitySegmentManager {
    return BatchedCapacitySegmentManager(
        store = store,
        validator = defaultBatchedCapacitySegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
