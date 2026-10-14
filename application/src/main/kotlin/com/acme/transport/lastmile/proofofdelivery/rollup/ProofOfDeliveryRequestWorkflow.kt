package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery requests through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryRequestService(
    var store: ProofOfDeliveryRequestStore,
    var validator: ProofOfDeliveryRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("proof of delivery request $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newProofOfDeliveryRequestService(store: ProofOfDeliveryRequestStore, clock: Clock): ProofOfDeliveryRequestService {
    return ProofOfDeliveryRequestService(
        store = store,
        validator = defaultProofOfDeliveryRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
