package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery drafts through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryDraftWorkflow(
    var store: ProofOfDeliveryDraftStore,
    var validator: ProofOfDeliveryDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("proof of delivery draft $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryDraftStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryDraftStatus.EXPIRED)
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

fun newProofOfDeliveryDraftWorkflow(store: ProofOfDeliveryDraftStore, clock: Clock): ProofOfDeliveryDraftWorkflow {
    return ProofOfDeliveryDraftWorkflow(
        store = store,
        validator = defaultProofOfDeliveryDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
