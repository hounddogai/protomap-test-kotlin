package com.acme.transport.safety.scorecard.quota

import com.acme.transport.safety.clock.Clock

/** Moves global scorecard budgets through their life cycle and keeps a journal of what it did. */
data class GlobalScorecardBudgetManager(
    var store: GlobalScorecardBudgetStore,
    var validator: GlobalScorecardBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalScorecardBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global scorecard budget $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalScorecardBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global scorecard budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalScorecardBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global scorecard budget $code not found")
        val result = engine.price(item)
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

fun newGlobalScorecardBudgetManager(store: GlobalScorecardBudgetStore, clock: Clock): GlobalScorecardBudgetManager {
    return GlobalScorecardBudgetManager(
        store = store,
        validator = defaultGlobalScorecardBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalScorecardBudgetEngine(),
    )
}
