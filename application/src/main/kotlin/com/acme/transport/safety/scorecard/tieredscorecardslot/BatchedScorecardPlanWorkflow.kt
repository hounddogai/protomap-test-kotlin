package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves batched scorecard plans through their life cycle and keeps a journal of what it did. */
data class BatchedScorecardPlanDesk(
    var store: BatchedScorecardPlanStore,
    var validator: BatchedScorecardPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedScorecardPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched scorecard plan $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedScorecardPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched scorecard plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedScorecardPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched scorecard plan $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newBatchedScorecardPlanDesk(store: BatchedScorecardPlanStore, clock: Clock, flags: Registry): BatchedScorecardPlanDesk {
    return BatchedScorecardPlanDesk(
        store = store,
        validator = defaultBatchedScorecardPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedScorecardPlanEngine(flags),
    )
}
