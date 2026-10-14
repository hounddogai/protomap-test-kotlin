package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves rolling proof of delivery preferences through their life cycle and keeps a journal of what it did.
 */
data class RollingProofOfDeliveryPreferenceCoordinator(
    var store: RollingProofOfDeliveryPreferenceStore,
    var validator: RollingProofOfDeliveryPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingProofOfDeliveryPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling proof of delivery preference $code not found")
        validator.requireValid(item)
        item.moveTo(RollingProofOfDeliveryPreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling proof of delivery preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingProofOfDeliveryPreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling proof of delivery preference $code not found")
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

fun newRollingProofOfDeliveryPreferenceCoordinator(store: RollingProofOfDeliveryPreferenceStore, clock: Clock, flags: Registry): RollingProofOfDeliveryPreferenceCoordinator {
    return RollingProofOfDeliveryPreferenceCoordinator(
        store = store,
        validator = defaultRollingProofOfDeliveryPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingProofOfDeliveryPreferenceEngine(flags),
    )
}
