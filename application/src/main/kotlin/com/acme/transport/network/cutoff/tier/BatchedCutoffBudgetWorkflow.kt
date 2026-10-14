package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves batched cutoff budgets through their life cycle and keeps a journal of what it did. */
data class BatchedCutoffBudgetWorkflow(
    var store: BatchedCutoffBudgetStore,
    var validator: BatchedCutoffBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedCutoffBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched cutoff budget $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedCutoffBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched cutoff budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedCutoffBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched cutoff budget $code not found")
        val result = engine.price(item)
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

fun newBatchedCutoffBudgetWorkflow(store: BatchedCutoffBudgetStore, clock: Clock, flags: Registry): BatchedCutoffBudgetWorkflow {
    return BatchedCutoffBudgetWorkflow(
        store = store,
        validator = defaultBatchedCutoffBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedCutoffBudgetEngine(flags),
    )
}
