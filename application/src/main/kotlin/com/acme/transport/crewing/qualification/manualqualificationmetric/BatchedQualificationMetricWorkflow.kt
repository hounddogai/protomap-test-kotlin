package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched qualification metrics through their life cycle and keeps a journal of what it did. */
data class BatchedQualificationMetricService(
    var store: BatchedQualificationMetricStore,
    var validator: BatchedQualificationMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedQualificationMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched qualification metric $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedQualificationMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched qualification metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedQualificationMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched qualification metric $code not found")
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

fun newBatchedQualificationMetricService(store: BatchedQualificationMetricStore, clock: Clock, flags: Registry): BatchedQualificationMetricService {
    return BatchedQualificationMetricService(
        store = store,
        validator = defaultBatchedQualificationMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedQualificationMetricEngine(flags),
    )
}
