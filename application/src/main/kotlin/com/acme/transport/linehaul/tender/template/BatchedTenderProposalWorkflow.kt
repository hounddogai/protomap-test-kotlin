package com.acme.transport.linehaul.tender.template

import com.acme.transport.linehaul.clock.Clock

/** Moves batched tender proposals through their life cycle and keeps a journal of what it did. */
data class BatchedTenderProposalManager(
    var store: BatchedTenderProposalStore,
    var validator: BatchedTenderProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched tender proposal $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTenderProposalStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched tender proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTenderProposalStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newBatchedTenderProposalManager(store: BatchedTenderProposalStore, clock: Clock): BatchedTenderProposalManager {
    return BatchedTenderProposalManager(
        store = store,
        validator = defaultBatchedTenderProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
