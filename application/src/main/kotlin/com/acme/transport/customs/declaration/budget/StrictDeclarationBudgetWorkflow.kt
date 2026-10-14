package com.acme.transport.customs.declaration.budget

import com.acme.transport.customs.clock.Clock

/** Moves strict declaration budgets through their life cycle and keeps a journal of what it did. */
data class StrictDeclarationBudgetManager(
    var store: StrictDeclarationBudgetStore,
    var validator: StrictDeclarationBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict declaration budget $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDeclarationBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict declaration budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDeclarationBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newStrictDeclarationBudgetManager(store: StrictDeclarationBudgetStore, clock: Clock): StrictDeclarationBudgetManager {
    return StrictDeclarationBudgetManager(
        store = store,
        validator = defaultStrictDeclarationBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
