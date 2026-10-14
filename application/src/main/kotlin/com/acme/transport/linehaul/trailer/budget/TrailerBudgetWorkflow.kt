package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves trailer budgets through their life cycle and keeps a journal of what it did. */
data class TrailerBudgetWorkflow(
    var store: TrailerBudgetStore,
    var validator: TrailerBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("trailer budget $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("trailer budget $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newTrailerBudgetWorkflow(store: TrailerBudgetStore, clock: Clock, flags: Registry): TrailerBudgetWorkflow {
    return TrailerBudgetWorkflow(
        store = store,
        validator = defaultTrailerBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerBudgetEngine(flags),
    )
}
