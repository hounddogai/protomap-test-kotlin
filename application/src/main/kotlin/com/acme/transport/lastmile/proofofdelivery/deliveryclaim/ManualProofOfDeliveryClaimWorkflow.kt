package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves manual proof of delivery claims through their life cycle and keeps a journal of what it did.
 */
data class ManualProofOfDeliveryClaimDesk(
    var store: ManualProofOfDeliveryClaimStore,
    var validator: ManualProofOfDeliveryClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualProofOfDeliveryClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual proof of delivery claim $code not found")
        validator.requireValid(item)
        item.moveTo(ManualProofOfDeliveryClaimStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual proof of delivery claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualProofOfDeliveryClaimStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual proof of delivery claim $code not found")
        val result = engine.run(item)
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

fun newManualProofOfDeliveryClaimDesk(store: ManualProofOfDeliveryClaimStore, clock: Clock, flags: Registry): ManualProofOfDeliveryClaimDesk {
    return ManualProofOfDeliveryClaimDesk(
        store = store,
        validator = defaultManualProofOfDeliveryClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualProofOfDeliveryClaimEngine(flags),
    )
}
