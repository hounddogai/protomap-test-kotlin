package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.lastmile.clock.Clock

/** Moves batched drop budgets through their life cycle and keeps a journal of what it did. */
data class BatchedDropBudgetWorkflow(
    var store: BatchedDropBudgetStore,
    var validator: BatchedDropBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDropBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched drop budget $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDropBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched drop budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDropBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched drop budget $code not found")
        val result = engine.evaluate(item)
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

fun newBatchedDropBudgetWorkflow(store: BatchedDropBudgetStore, clock: Clock): BatchedDropBudgetWorkflow {
    return BatchedDropBudgetWorkflow(
        store = store,
        validator = defaultBatchedDropBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDropBudgetEngine(),
    )
}
