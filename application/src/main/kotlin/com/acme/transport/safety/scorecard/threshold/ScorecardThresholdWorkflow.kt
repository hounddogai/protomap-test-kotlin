package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.safety.clock.Clock

/** Moves scorecard thresholds through their life cycle and keeps a journal of what it did. */
data class ScorecardThresholdService(
    var store: ScorecardThresholdStore,
    var validator: ScorecardThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScorecardThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scorecard threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scorecard threshold $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newScorecardThresholdService(store: ScorecardThresholdStore, clock: Clock): ScorecardThresholdService {
    return ScorecardThresholdService(
        store = store,
        validator = defaultScorecardThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScorecardThresholdEngine(),
    )
}
