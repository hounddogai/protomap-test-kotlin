package com.acme.transport.crewing.swap.budget

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves swap budgets through their life cycle and keeps a journal of what it did. */
data class SwapBudgetWorkflow(
    var store: SwapBudgetStore,
    var validator: SwapBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("swap budget $code not found")
        validator.requireValid(item)
        item.moveTo(SwapBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("swap budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("swap budget $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newSwapBudgetWorkflow(store: SwapBudgetStore, clock: Clock, flags: Registry): SwapBudgetWorkflow {
    return SwapBudgetWorkflow(
        store = store,
        validator = defaultSwapBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapBudgetEngine(flags),
    )
}
