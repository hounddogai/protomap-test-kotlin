package com.acme.transport.safety.scorecard.window

import com.acme.transport.safety.clock.Clock

/** Moves scorecard batches through their life cycle and keeps a journal of what it did. */
data class ScorecardBatchCoordinator(
    var store: ScorecardBatchStore,
    var validator: ScorecardBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScorecardBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scorecard batch $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardBatchStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardBatchStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scorecard batch $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newScorecardBatchCoordinator(store: ScorecardBatchStore, clock: Clock): ScorecardBatchCoordinator {
    return ScorecardBatchCoordinator(
        store = store,
        validator = defaultScorecardBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScorecardBatchEngine(),
    )
}
