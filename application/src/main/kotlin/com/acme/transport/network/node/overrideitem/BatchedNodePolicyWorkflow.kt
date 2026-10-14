package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves batched node policies through their life cycle and keeps a journal of what it did. */
data class BatchedNodePolicyWorkflow(
    var store: BatchedNodePolicyStore,
    var validator: BatchedNodePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedNodePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched node policy $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedNodePolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched node policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedNodePolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched node policy $code not found")
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

fun newBatchedNodePolicyWorkflow(store: BatchedNodePolicyStore, clock: Clock, flags: Registry): BatchedNodePolicyWorkflow {
    return BatchedNodePolicyWorkflow(
        store = store,
        validator = defaultBatchedNodePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedNodePolicyEngine(flags),
    )
}
