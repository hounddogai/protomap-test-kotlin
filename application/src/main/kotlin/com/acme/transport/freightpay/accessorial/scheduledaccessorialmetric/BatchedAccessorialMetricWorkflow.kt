package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.freightpay.clock.Clock

/** Moves batched accessorial metrics through their life cycle and keeps a journal of what it did. */
data class BatchedAccessorialMetricManager(
    var store: BatchedAccessorialMetricStore,
    var validator: BatchedAccessorialMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched accessorial metric $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAccessorialMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched accessorial metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAccessorialMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newBatchedAccessorialMetricManager(store: BatchedAccessorialMetricStore, clock: Clock): BatchedAccessorialMetricManager {
    return BatchedAccessorialMetricManager(
        store = store,
        validator = defaultBatchedAccessorialMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
