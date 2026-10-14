package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.safety.clock.Clock

/** Moves batched scorecard proposals through their life cycle and keeps a journal of what it did. */
data class BatchedScorecardProposalManager(
    var store: BatchedScorecardProposalStore,
    var validator: BatchedScorecardProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched scorecard proposal $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedScorecardProposalStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched scorecard proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedScorecardProposalStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newBatchedScorecardProposalManager(store: BatchedScorecardProposalStore, clock: Clock): BatchedScorecardProposalManager {
    return BatchedScorecardProposalManager(
        store = store,
        validator = defaultBatchedScorecardProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
