package com.acme.transport.network.node.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves batched node requests through their life cycle and keeps a journal of what it did. */
data class BatchedNodeRequestService(
    var store: BatchedNodeRequestStore,
    var validator: BatchedNodeRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedNodeRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched node request $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedNodeRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched node request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedNodeRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched node request $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newBatchedNodeRequestService(store: BatchedNodeRequestStore, clock: Clock, flags: Registry): BatchedNodeRequestService {
    return BatchedNodeRequestService(
        store = store,
        validator = defaultBatchedNodeRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedNodeRequestEngine(flags),
    )
}
