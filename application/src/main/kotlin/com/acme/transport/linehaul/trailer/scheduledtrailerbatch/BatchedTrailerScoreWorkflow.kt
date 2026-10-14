package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.linehaul.clock.Clock

/** Moves batched trailer scores through their life cycle and keeps a journal of what it did. */
data class BatchedTrailerScoreService(
    var store: BatchedTrailerScoreStore,
    var validator: BatchedTrailerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched trailer score $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTrailerScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched trailer score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTrailerScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newBatchedTrailerScoreService(store: BatchedTrailerScoreStore, clock: Clock): BatchedTrailerScoreService {
    return BatchedTrailerScoreService(
        store = store,
        validator = defaultBatchedTrailerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
