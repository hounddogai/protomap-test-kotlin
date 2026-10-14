package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery policies through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryPolicyDesk(
    var store: ProofOfDeliveryPolicyStore,
    var validator: ProofOfDeliveryPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("proof of delivery policy $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryPolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryPolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newProofOfDeliveryPolicyDesk(store: ProofOfDeliveryPolicyStore, clock: Clock): ProofOfDeliveryPolicyDesk {
    return ProofOfDeliveryPolicyDesk(
        store = store,
        validator = defaultProofOfDeliveryPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
