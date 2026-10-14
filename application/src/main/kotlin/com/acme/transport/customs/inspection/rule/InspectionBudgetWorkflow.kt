package com.acme.transport.customs.inspection.rule

import com.acme.transport.customs.clock.Clock

/** Moves inspection budgets through their life cycle and keeps a journal of what it did. */
data class InspectionBudgetDesk(
    var store: InspectionBudgetStore,
    var validator: InspectionBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection budget $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newInspectionBudgetDesk(store: InspectionBudgetStore, clock: Clock): InspectionBudgetDesk {
    return InspectionBudgetDesk(
        store = store,
        validator = defaultInspectionBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
