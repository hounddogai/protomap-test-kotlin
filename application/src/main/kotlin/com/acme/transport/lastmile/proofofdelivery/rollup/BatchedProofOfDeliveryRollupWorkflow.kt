package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves batched proof of delivery rollups through their life cycle and keeps a journal of what it did.
 */
data class BatchedProofOfDeliveryRollupCoordinator(
    var store: BatchedProofOfDeliveryRollupStore,
    var validator: BatchedProofOfDeliveryRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedProofOfDeliveryRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched proof of delivery rollup $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedProofOfDeliveryRollupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched proof of delivery rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedProofOfDeliveryRollupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched proof of delivery rollup $code not found")
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

fun newBatchedProofOfDeliveryRollupCoordinator(store: BatchedProofOfDeliveryRollupStore, clock: Clock, flags: Registry): BatchedProofOfDeliveryRollupCoordinator {
    return BatchedProofOfDeliveryRollupCoordinator(
        store = store,
        validator = defaultBatchedProofOfDeliveryRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedProofOfDeliveryRollupEngine(flags),
    )
}
