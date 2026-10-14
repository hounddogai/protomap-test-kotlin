package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.safety.clock.Clock

/** Moves batched near miss variances through their life cycle and keeps a journal of what it did. */
data class BatchedNearMissVarianceWorkflow(
    var store: BatchedNearMissVarianceStore,
    var validator: BatchedNearMissVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedNearMissVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched near miss variance $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedNearMissVarianceStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched near miss variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedNearMissVarianceStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched near miss variance $code not found")
        val result = engine.run(item)
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

fun newBatchedNearMissVarianceWorkflow(store: BatchedNearMissVarianceStore, clock: Clock): BatchedNearMissVarianceWorkflow {
    return BatchedNearMissVarianceWorkflow(
        store = store,
        validator = defaultBatchedNearMissVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedNearMissVarianceEngine(),
    )
}
