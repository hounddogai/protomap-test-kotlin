package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched rest break reviews through their life cycle and keeps a journal of what it did. */
data class BatchedRestBreakReviewCoordinator(
    var store: BatchedRestBreakReviewStore,
    var validator: BatchedRestBreakReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedRestBreakReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched rest break review $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedRestBreakReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched rest break review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedRestBreakReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched rest break review $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newBatchedRestBreakReviewCoordinator(store: BatchedRestBreakReviewStore, clock: Clock, flags: Registry): BatchedRestBreakReviewCoordinator {
    return BatchedRestBreakReviewCoordinator(
        store = store,
        validator = defaultBatchedRestBreakReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedRestBreakReviewEngine(flags),
    )
}
