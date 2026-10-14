package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.clock.Clock

/**
 * Moves batched route offset assignments through their life cycle and keeps a journal of what it did.
 */
data class BatchedRouteOffsetAssignmentManager(
    var store: BatchedRouteOffsetAssignmentStore,
    var validator: BatchedRouteOffsetAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedRouteOffsetAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched route offset assignment $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedRouteOffsetAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched route offset assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedRouteOffsetAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched route offset assignment $code not found")
        val result = engine.run(item)
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

fun newBatchedRouteOffsetAssignmentManager(store: BatchedRouteOffsetAssignmentStore, clock: Clock): BatchedRouteOffsetAssignmentManager {
    return BatchedRouteOffsetAssignmentManager(
        store = store,
        validator = defaultBatchedRouteOffsetAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedRouteOffsetAssignmentEngine(),
    )
}
