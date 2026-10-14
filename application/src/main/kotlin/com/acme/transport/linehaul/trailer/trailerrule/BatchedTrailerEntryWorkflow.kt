package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.linehaul.clock.Clock

/** Moves batched trailer entries through their life cycle and keeps a journal of what it did. */
data class BatchedTrailerEntryService(
    var store: BatchedTrailerEntryStore,
    var validator: BatchedTrailerEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched trailer entry $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTrailerEntryStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched trailer entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTrailerEntryStatus.STOPPED)
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

fun newBatchedTrailerEntryService(store: BatchedTrailerEntryStore, clock: Clock): BatchedTrailerEntryService {
    return BatchedTrailerEntryService(
        store = store,
        validator = defaultBatchedTrailerEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
