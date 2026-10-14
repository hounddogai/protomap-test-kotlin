package com.acme.transport.crewing.qualification.summary

import com.acme.transport.crewing.clock.Clock

/**
 * Moves batched qualification assignments through their life cycle and keeps a journal of what it did.
 */
data class BatchedQualificationAssignmentCoordinator(
    var store: BatchedQualificationAssignmentStore,
    var validator: BatchedQualificationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedQualificationAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched qualification assignment $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedQualificationAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched qualification assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedQualificationAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched qualification assignment $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newBatchedQualificationAssignmentCoordinator(store: BatchedQualificationAssignmentStore, clock: Clock): BatchedQualificationAssignmentCoordinator {
    return BatchedQualificationAssignmentCoordinator(
        store = store,
        validator = defaultBatchedQualificationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedQualificationAssignmentEngine(),
    )
}
