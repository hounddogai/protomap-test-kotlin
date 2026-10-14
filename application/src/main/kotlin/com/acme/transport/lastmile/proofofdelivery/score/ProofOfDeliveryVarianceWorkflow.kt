package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery variances through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryVarianceManager(
    var store: ProofOfDeliveryVarianceStore,
    var validator: ProofOfDeliveryVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("proof of delivery variance $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newProofOfDeliveryVarianceManager(store: ProofOfDeliveryVarianceStore, clock: Clock): ProofOfDeliveryVarianceManager {
    return ProofOfDeliveryVarianceManager(
        store = store,
        validator = defaultProofOfDeliveryVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
