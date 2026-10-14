package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves batched lane plan thresholds through their life cycle and keeps a journal of what it did. */
data class BatchedLanePlanThresholdWorkflow(
    var store: BatchedLanePlanThresholdStore,
    var validator: BatchedLanePlanThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedLanePlanThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched lane plan threshold $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedLanePlanThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched lane plan threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedLanePlanThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched lane plan threshold $code not found")
        val result = engine.run(item)
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

fun newBatchedLanePlanThresholdWorkflow(store: BatchedLanePlanThresholdStore, clock: Clock, flags: Registry): BatchedLanePlanThresholdWorkflow {
    return BatchedLanePlanThresholdWorkflow(
        store = store,
        validator = defaultBatchedLanePlanThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedLanePlanThresholdEngine(flags),
    )
}
