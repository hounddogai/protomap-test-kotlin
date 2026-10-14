package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.clock.Clock

/** Moves batched accessorial rollups through their life cycle and keeps a journal of what it did. */
data class BatchedAccessorialRollupManager(
    var store: BatchedAccessorialRollupStore,
    var validator: BatchedAccessorialRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched accessorial rollup $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAccessorialRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched accessorial rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAccessorialRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newBatchedAccessorialRollupManager(store: BatchedAccessorialRollupStore, clock: Clock): BatchedAccessorialRollupManager {
    return BatchedAccessorialRollupManager(
        store = store,
        validator = defaultBatchedAccessorialRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
