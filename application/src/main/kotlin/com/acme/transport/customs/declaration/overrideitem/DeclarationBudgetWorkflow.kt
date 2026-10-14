package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.clock.Clock

/** Moves declaration budgets through their life cycle and keeps a journal of what it did. */
data class DeclarationBudgetManager(
    var store: DeclarationBudgetStore,
    var validator: DeclarationBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("declaration budget $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("declaration budget $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newDeclarationBudgetManager(store: DeclarationBudgetStore, clock: Clock): DeclarationBudgetManager {
    return DeclarationBudgetManager(
        store = store,
        validator = defaultDeclarationBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationBudgetEngine(),
    )
}
