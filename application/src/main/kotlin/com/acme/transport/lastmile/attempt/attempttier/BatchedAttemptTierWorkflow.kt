package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.lastmile.clock.Clock

/** Moves batched attempt tiers through their life cycle and keeps a journal of what it did. */
data class BatchedAttemptTierWorkflow(
    var store: BatchedAttemptTierStore,
    var validator: BatchedAttemptTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAttemptTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched attempt tier $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAttemptTierStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched attempt tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAttemptTierStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched attempt tier $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newBatchedAttemptTierWorkflow(store: BatchedAttemptTierStore, clock: Clock): BatchedAttemptTierWorkflow {
    return BatchedAttemptTierWorkflow(
        store = store,
        validator = defaultBatchedAttemptTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAttemptTierEngine(),
    )
}
