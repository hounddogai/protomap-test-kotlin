package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery thresholds through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryThresholdCoordinator(
    var store: ProofOfDeliveryThresholdStore,
    var validator: ProofOfDeliveryThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("proof of delivery threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newProofOfDeliveryThresholdCoordinator(store: ProofOfDeliveryThresholdStore, clock: Clock): ProofOfDeliveryThresholdCoordinator {
    return ProofOfDeliveryThresholdCoordinator(
        store = store,
        validator = defaultProofOfDeliveryThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
