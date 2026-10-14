package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.fleetops.clock.Clock

/** Moves batched defect proposals through their life cycle and keeps a journal of what it did. */
data class BatchedDefectProposalManager(
    var store: BatchedDefectProposalStore,
    var validator: BatchedDefectProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched defect proposal $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDefectProposalStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched defect proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDefectProposalStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newBatchedDefectProposalManager(store: BatchedDefectProposalStore, clock: Clock): BatchedDefectProposalManager {
    return BatchedDefectProposalManager(
        store = store,
        validator = defaultBatchedDefectProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
