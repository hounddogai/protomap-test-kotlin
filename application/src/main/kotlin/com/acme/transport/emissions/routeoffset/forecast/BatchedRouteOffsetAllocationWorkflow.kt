package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.clock.Clock

/**
 * Moves batched route offset allocations through their life cycle and keeps a journal of what it did.
 */
data class BatchedRouteOffsetAllocationWorkflow(
    var store: BatchedRouteOffsetAllocationStore,
    var validator: BatchedRouteOffsetAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedRouteOffsetAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched route offset allocation $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedRouteOffsetAllocationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched route offset allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedRouteOffsetAllocationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched route offset allocation $code not found")
        val result = engine.compute(item)
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

fun newBatchedRouteOffsetAllocationWorkflow(store: BatchedRouteOffsetAllocationStore, clock: Clock): BatchedRouteOffsetAllocationWorkflow {
    return BatchedRouteOffsetAllocationWorkflow(
        store = store,
        validator = defaultBatchedRouteOffsetAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedRouteOffsetAllocationEngine(),
    )
}
