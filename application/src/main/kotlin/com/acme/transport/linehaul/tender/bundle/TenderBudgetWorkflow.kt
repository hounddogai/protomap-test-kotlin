package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.linehaul.clock.Clock

/** Moves tender budgets through their life cycle and keeps a journal of what it did. */
data class TenderBudgetService(
    var store: TenderBudgetStore,
    var validator: TenderBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tender budget $code not found")
        validator.requireValid(item)
        item.moveTo(TenderBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tender budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newTenderBudgetService(store: TenderBudgetStore, clock: Clock): TenderBudgetService {
    return TenderBudgetService(
        store = store,
        validator = defaultTenderBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
