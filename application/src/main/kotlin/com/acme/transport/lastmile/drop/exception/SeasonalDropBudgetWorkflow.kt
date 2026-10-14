package com.acme.transport.lastmile.drop.exception

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal drop budgets through their life cycle and keeps a journal of what it did. */
data class SeasonalDropBudgetManager(
    var store: SeasonalDropBudgetStore,
    var validator: SeasonalDropBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal drop budget $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDropBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal drop budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDropBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newSeasonalDropBudgetManager(store: SeasonalDropBudgetStore, clock: Clock): SeasonalDropBudgetManager {
    return SeasonalDropBudgetManager(
        store = store,
        validator = defaultSeasonalDropBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
