package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves batched qualification checkpoints through their life cycle and keeps a journal of what it did.
 */
data class BatchedQualificationCheckpointManager(
    var store: BatchedQualificationCheckpointStore,
    var validator: BatchedQualificationCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedQualificationCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched qualification checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedQualificationCheckpointStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched qualification checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedQualificationCheckpointStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched qualification checkpoint $code not found")
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

fun newBatchedQualificationCheckpointManager(store: BatchedQualificationCheckpointStore, clock: Clock, flags: Registry): BatchedQualificationCheckpointManager {
    return BatchedQualificationCheckpointManager(
        store = store,
        validator = defaultBatchedQualificationCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedQualificationCheckpointEngine(flags),
    )
}
