package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves weighted proof of delivery segments through their life cycle and keeps a journal of what it did.
 */
data class WeightedProofOfDeliverySegmentManager(
    var store: WeightedProofOfDeliverySegmentStore,
    var validator: WeightedProofOfDeliverySegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedProofOfDeliverySegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted proof of delivery segment $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedProofOfDeliverySegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted proof of delivery segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedProofOfDeliverySegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted proof of delivery segment $code not found")
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

fun newWeightedProofOfDeliverySegmentManager(store: WeightedProofOfDeliverySegmentStore, clock: Clock, flags: Registry): WeightedProofOfDeliverySegmentManager {
    return WeightedProofOfDeliverySegmentManager(
        store = store,
        validator = defaultWeightedProofOfDeliverySegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedProofOfDeliverySegmentEngine(flags),
    )
}
