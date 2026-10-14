package com.acme.transport.customs.hold.bundle

import com.acme.transport.customs.clock.Clock

/** Moves hold budgets through their life cycle and keeps a journal of what it did. */
data class HoldBudgetCoordinator(
    var store: HoldBudgetStore,
    var validator: HoldBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("hold budget $code not found")
        validator.requireValid(item)
        item.moveTo(HoldBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hold budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newHoldBudgetCoordinator(store: HoldBudgetStore, clock: Clock): HoldBudgetCoordinator {
    return HoldBudgetCoordinator(
        store = store,
        validator = defaultHoldBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
