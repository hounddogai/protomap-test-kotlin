package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.clock.Clock

/** Moves seasonal defect budgets through their life cycle and keeps a journal of what it did. */
data class SeasonalDefectBudgetWorkflow(
    var store: SeasonalDefectBudgetStore,
    var validator: SeasonalDefectBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal defect budget $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDefectBudgetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal defect budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDefectBudgetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newSeasonalDefectBudgetWorkflow(store: SeasonalDefectBudgetStore, clock: Clock): SeasonalDefectBudgetWorkflow {
    return SeasonalDefectBudgetWorkflow(
        store = store,
        validator = defaultSeasonalDefectBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
