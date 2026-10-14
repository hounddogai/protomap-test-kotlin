package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery claims through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryClaimService(
    var store: ProofOfDeliveryClaimStore,
    var validator: ProofOfDeliveryClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("proof of delivery claim $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryClaimStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryClaimStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newProofOfDeliveryClaimService(store: ProofOfDeliveryClaimStore, clock: Clock): ProofOfDeliveryClaimService {
    return ProofOfDeliveryClaimService(
        store = store,
        validator = defaultProofOfDeliveryClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
