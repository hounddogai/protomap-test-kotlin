package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched rate card milestones through their life cycle and keeps a journal of what it did. */
data class BatchedRateCardMilestoneCoordinator(
    var store: BatchedRateCardMilestoneStore,
    var validator: BatchedRateCardMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedRateCardMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched rate card milestone $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedRateCardMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched rate card milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedRateCardMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched rate card milestone $code not found")
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

fun newBatchedRateCardMilestoneCoordinator(store: BatchedRateCardMilestoneStore, clock: Clock, flags: Registry): BatchedRateCardMilestoneCoordinator {
    return BatchedRateCardMilestoneCoordinator(
        store = store,
        validator = defaultBatchedRateCardMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedRateCardMilestoneEngine(flags),
    )
}
