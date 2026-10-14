package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery assignments through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryAssignmentCoordinator(
    var store: ProofOfDeliveryAssignmentStore,
    var validator: ProofOfDeliveryAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("proof of delivery assignment $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newProofOfDeliveryAssignmentCoordinator(store: ProofOfDeliveryAssignmentStore, clock: Clock): ProofOfDeliveryAssignmentCoordinator {
    return ProofOfDeliveryAssignmentCoordinator(
        store = store,
        validator = defaultProofOfDeliveryAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
