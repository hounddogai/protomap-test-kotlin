package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.lastmile.clock.Clock

/** Moves manual redelivery budgets through their life cycle and keeps a journal of what it did. */
data class ManualRedeliveryBudgetCoordinator(
    var store: ManualRedeliveryBudgetStore,
    var validator: ManualRedeliveryBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual redelivery budget $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRedeliveryBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual redelivery budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRedeliveryBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newManualRedeliveryBudgetCoordinator(store: ManualRedeliveryBudgetStore, clock: Clock): ManualRedeliveryBudgetCoordinator {
    return ManualRedeliveryBudgetCoordinator(
        store = store,
        validator = defaultManualRedeliveryBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
