package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves batched proof of delivery budgets through their life cycle and keeps a journal of what it did.
 */
data class BatchedProofOfDeliveryBudgetCoordinator(
    var store: BatchedProofOfDeliveryBudgetStore,
    var validator: BatchedProofOfDeliveryBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedProofOfDeliveryBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched proof of delivery budget $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedProofOfDeliveryBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched proof of delivery budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedProofOfDeliveryBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched proof of delivery budget $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newBatchedProofOfDeliveryBudgetCoordinator(store: BatchedProofOfDeliveryBudgetStore, clock: Clock, flags: Registry): BatchedProofOfDeliveryBudgetCoordinator {
    return BatchedProofOfDeliveryBudgetCoordinator(
        store = store,
        validator = defaultBatchedProofOfDeliveryBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedProofOfDeliveryBudgetEngine(flags),
    )
}
