package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.safety.clock.Clock

/** Moves batched scorecard bundles through their life cycle and keeps a journal of what it did. */
data class BatchedScorecardBundleManager(
    var store: BatchedScorecardBundleStore,
    var validator: BatchedScorecardBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedScorecardBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched scorecard bundle $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedScorecardBundleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched scorecard bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedScorecardBundleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched scorecard bundle $code not found")
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

fun newBatchedScorecardBundleManager(store: BatchedScorecardBundleStore, clock: Clock): BatchedScorecardBundleManager {
    return BatchedScorecardBundleManager(
        store = store,
        validator = defaultBatchedScorecardBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedScorecardBundleEngine(),
    )
}
