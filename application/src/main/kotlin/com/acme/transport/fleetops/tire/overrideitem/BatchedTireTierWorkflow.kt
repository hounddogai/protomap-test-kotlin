package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.fleetops.clock.Clock

/** Moves batched tire tiers through their life cycle and keeps a journal of what it did. */
data class BatchedTireTierWorkflow(
    var store: BatchedTireTierStore,
    var validator: BatchedTireTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedTireTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched tire tier $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTireTierStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched tire tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTireTierStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched tire tier $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newBatchedTireTierWorkflow(store: BatchedTireTierStore, clock: Clock): BatchedTireTierWorkflow {
    return BatchedTireTierWorkflow(
        store = store,
        validator = defaultBatchedTireTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedTireTierEngine(),
    )
}
