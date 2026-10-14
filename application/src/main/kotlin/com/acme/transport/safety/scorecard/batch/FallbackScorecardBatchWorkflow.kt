package com.acme.transport.safety.scorecard.batch

import com.acme.transport.safety.clock.Clock

/** Moves fallback scorecard batches through their life cycle and keeps a journal of what it did. */
data class FallbackScorecardBatchManager(
    var store: FallbackScorecardBatchStore,
    var validator: FallbackScorecardBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackScorecardBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback scorecard batch $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackScorecardBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback scorecard batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackScorecardBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback scorecard batch $code not found")
        val result = engine.evaluate(item)
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

fun newFallbackScorecardBatchManager(store: FallbackScorecardBatchStore, clock: Clock): FallbackScorecardBatchManager {
    return FallbackScorecardBatchManager(
        store = store,
        validator = defaultFallbackScorecardBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackScorecardBatchEngine(),
    )
}
