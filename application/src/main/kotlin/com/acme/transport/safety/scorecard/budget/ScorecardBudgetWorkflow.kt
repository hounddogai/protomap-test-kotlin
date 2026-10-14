package com.acme.transport.safety.scorecard.budget

import com.acme.transport.safety.clock.Clock

/** Moves scorecard budgets through their life cycle and keeps a journal of what it did. */
data class ScorecardBudgetDesk(
    var store: ScorecardBudgetStore,
    var validator: ScorecardBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scorecard budget $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newScorecardBudgetDesk(store: ScorecardBudgetStore, clock: Clock): ScorecardBudgetDesk {
    return ScorecardBudgetDesk(
        store = store,
        validator = defaultScorecardBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
