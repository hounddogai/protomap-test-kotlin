package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity budgets through their life cycle and keeps a journal of what it did. */
data class CapacityBudgetDesk(
    var store: CapacityBudgetStore,
    var validator: CapacityBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capacity budget $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCapacityBudgetDesk(store: CapacityBudgetStore, clock: Clock): CapacityBudgetDesk {
    return CapacityBudgetDesk(
        store = store,
        validator = defaultCapacityBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
