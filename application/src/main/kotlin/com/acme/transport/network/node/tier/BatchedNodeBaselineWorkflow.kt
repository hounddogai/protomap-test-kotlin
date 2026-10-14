package com.acme.transport.network.node.tier

import com.acme.transport.network.clock.Clock

/** Moves batched node baselines through their life cycle and keeps a journal of what it did. */
data class BatchedNodeBaselineWorkflow(
    var store: BatchedNodeBaselineStore,
    var validator: BatchedNodeBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched node baseline $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedNodeBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched node baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedNodeBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newBatchedNodeBaselineWorkflow(store: BatchedNodeBaselineStore, clock: Clock): BatchedNodeBaselineWorkflow {
    return BatchedNodeBaselineWorkflow(
        store = store,
        validator = defaultBatchedNodeBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
