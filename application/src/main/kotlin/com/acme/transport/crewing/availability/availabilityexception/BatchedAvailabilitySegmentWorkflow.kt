package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched availability segments through their life cycle and keeps a journal of what it did. */
data class BatchedAvailabilitySegmentWorkflow(
    var store: BatchedAvailabilitySegmentStore,
    var validator: BatchedAvailabilitySegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAvailabilitySegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched availability segment $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAvailabilitySegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched availability segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAvailabilitySegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched availability segment $code not found")
        val result = engine.evaluate(item)
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

fun newBatchedAvailabilitySegmentWorkflow(store: BatchedAvailabilitySegmentStore, clock: Clock, flags: Registry): BatchedAvailabilitySegmentWorkflow {
    return BatchedAvailabilitySegmentWorkflow(
        store = store,
        validator = defaultBatchedAvailabilitySegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAvailabilitySegmentEngine(flags),
    )
}
