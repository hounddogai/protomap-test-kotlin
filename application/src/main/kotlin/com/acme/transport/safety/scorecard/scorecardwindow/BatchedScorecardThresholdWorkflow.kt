package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.safety.clock.Clock

/** Moves batched scorecard thresholds through their life cycle and keeps a journal of what it did. */
data class BatchedScorecardThresholdCoordinator(
    var store: BatchedScorecardThresholdStore,
    var validator: BatchedScorecardThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedScorecardThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched scorecard threshold $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedScorecardThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched scorecard threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedScorecardThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched scorecard threshold $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newBatchedScorecardThresholdCoordinator(store: BatchedScorecardThresholdStore, clock: Clock): BatchedScorecardThresholdCoordinator {
    return BatchedScorecardThresholdCoordinator(
        store = store,
        validator = defaultBatchedScorecardThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedScorecardThresholdEngine(),
    )
}
