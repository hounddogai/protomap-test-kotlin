package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery quotas through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryQuotaWorkflow(
    var store: ProofOfDeliveryQuotaStore,
    var validator: ProofOfDeliveryQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("proof of delivery quota $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newProofOfDeliveryQuotaWorkflow(store: ProofOfDeliveryQuotaStore, clock: Clock): ProofOfDeliveryQuotaWorkflow {
    return ProofOfDeliveryQuotaWorkflow(
        store = store,
        validator = defaultProofOfDeliveryQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
