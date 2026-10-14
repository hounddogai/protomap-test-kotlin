package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched dispute batches through their life cycle and keeps a journal of what it did. */
data class BatchedDisputeBatchWorkflow(
    var store: BatchedDisputeBatchStore,
    var validator: BatchedDisputeBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDisputeBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched dispute batch $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDisputeBatchStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched dispute batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDisputeBatchStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched dispute batch $code not found")
        val result = engine.compute(item)
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

fun newBatchedDisputeBatchWorkflow(store: BatchedDisputeBatchStore, clock: Clock, flags: Registry): BatchedDisputeBatchWorkflow {
    return BatchedDisputeBatchWorkflow(
        store = store,
        validator = defaultBatchedDisputeBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDisputeBatchEngine(flags),
    )
}
