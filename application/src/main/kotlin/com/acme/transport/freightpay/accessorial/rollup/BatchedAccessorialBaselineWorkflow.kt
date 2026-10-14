package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.clock.Clock

/** Moves batched accessorial baselines through their life cycle and keeps a journal of what it did. */
data class BatchedAccessorialBaselineDesk(
    var store: BatchedAccessorialBaselineStore,
    var validator: BatchedAccessorialBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched accessorial baseline $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAccessorialBaselineStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched accessorial baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAccessorialBaselineStatus.CANCELLED)
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

fun newBatchedAccessorialBaselineDesk(store: BatchedAccessorialBaselineStore, clock: Clock): BatchedAccessorialBaselineDesk {
    return BatchedAccessorialBaselineDesk(
        store = store,
        validator = defaultBatchedAccessorialBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
