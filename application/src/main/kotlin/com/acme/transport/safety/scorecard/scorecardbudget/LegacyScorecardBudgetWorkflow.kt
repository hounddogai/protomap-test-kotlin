package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves legacy scorecard budgets through their life cycle and keeps a journal of what it did. */
data class LegacyScorecardBudgetDesk(
    var store: LegacyScorecardBudgetStore,
    var validator: LegacyScorecardBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyScorecardBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy scorecard budget $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyScorecardBudgetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy scorecard budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyScorecardBudgetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy scorecard budget $code not found")
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

fun newLegacyScorecardBudgetDesk(store: LegacyScorecardBudgetStore, clock: Clock, flags: Registry): LegacyScorecardBudgetDesk {
    return LegacyScorecardBudgetDesk(
        store = store,
        validator = defaultLegacyScorecardBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyScorecardBudgetEngine(flags),
    )
}
