package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves manual proof of delivery adjustments through their life cycle and keeps a journal of what it did.
 */
data class ManualProofOfDeliveryAdjustmentWorkflow(
    var store: ManualProofOfDeliveryAdjustmentStore,
    var validator: ManualProofOfDeliveryAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualProofOfDeliveryAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual proof of delivery adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ManualProofOfDeliveryAdjustmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual proof of delivery adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualProofOfDeliveryAdjustmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual proof of delivery adjustment $code not found")
        val result = engine.evaluate(item)
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

fun newManualProofOfDeliveryAdjustmentWorkflow(store: ManualProofOfDeliveryAdjustmentStore, clock: Clock): ManualProofOfDeliveryAdjustmentWorkflow {
    return ManualProofOfDeliveryAdjustmentWorkflow(
        store = store,
        validator = defaultManualProofOfDeliveryAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualProofOfDeliveryAdjustmentEngine(),
    )
}
