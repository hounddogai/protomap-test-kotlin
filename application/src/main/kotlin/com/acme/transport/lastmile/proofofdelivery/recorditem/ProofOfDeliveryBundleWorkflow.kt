package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery bundles through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryBundleService(
    var store: ProofOfDeliveryBundleStore,
    var validator: ProofOfDeliveryBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ProofOfDeliveryBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("proof of delivery bundle $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryBundleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryBundleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("proof of delivery bundle $code not found")
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

fun newProofOfDeliveryBundleService(store: ProofOfDeliveryBundleStore, clock: Clock, flags: Registry): ProofOfDeliveryBundleService {
    return ProofOfDeliveryBundleService(
        store = store,
        validator = defaultProofOfDeliveryBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultProofOfDeliveryBundleEngine(flags),
    )
}
