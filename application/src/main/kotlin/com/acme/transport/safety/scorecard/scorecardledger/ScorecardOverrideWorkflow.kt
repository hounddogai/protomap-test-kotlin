package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves scorecard overrides through their life cycle and keeps a journal of what it did. */
data class ScorecardOverrideCoordinator(
    var store: ScorecardOverrideStore,
    var validator: ScorecardOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScorecardOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scorecard override $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardOverrideStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardOverrideStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scorecard override $code not found")
        val result = engine.compute(item)
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

fun newScorecardOverrideCoordinator(store: ScorecardOverrideStore, clock: Clock, flags: Registry): ScorecardOverrideCoordinator {
    return ScorecardOverrideCoordinator(
        store = store,
        validator = defaultScorecardOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScorecardOverrideEngine(flags),
    )
}
