package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched utilization metrics through their life cycle and keeps a journal of what it did. */
data class BatchedUtilizationMetricWorkflow(
    var store: BatchedUtilizationMetricStore,
    var validator: BatchedUtilizationMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedUtilizationMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched utilization metric $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedUtilizationMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched utilization metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedUtilizationMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched utilization metric $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newBatchedUtilizationMetricWorkflow(store: BatchedUtilizationMetricStore, clock: Clock, flags: Registry): BatchedUtilizationMetricWorkflow {
    return BatchedUtilizationMetricWorkflow(
        store = store,
        validator = defaultBatchedUtilizationMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedUtilizationMetricEngine(flags),
    )
}
