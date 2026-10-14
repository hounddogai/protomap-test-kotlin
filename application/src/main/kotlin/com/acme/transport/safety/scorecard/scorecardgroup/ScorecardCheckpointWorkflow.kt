package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves scorecard checkpoints through their life cycle and keeps a journal of what it did. */
data class ScorecardCheckpointCoordinator(
    var store: ScorecardCheckpointStore,
    var validator: ScorecardCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScorecardCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scorecard checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardCheckpointStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardCheckpointStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scorecard checkpoint $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newScorecardCheckpointCoordinator(store: ScorecardCheckpointStore, clock: Clock, flags: Registry): ScorecardCheckpointCoordinator {
    return ScorecardCheckpointCoordinator(
        store = store,
        validator = defaultScorecardCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScorecardCheckpointEngine(flags),
    )
}
