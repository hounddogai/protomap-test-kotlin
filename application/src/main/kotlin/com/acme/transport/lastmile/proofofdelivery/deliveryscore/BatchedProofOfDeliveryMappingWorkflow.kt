package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves batched proof of delivery mappings through their life cycle and keeps a journal of what it did.
 */
data class BatchedProofOfDeliveryMappingCoordinator(
    var store: BatchedProofOfDeliveryMappingStore,
    var validator: BatchedProofOfDeliveryMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched proof of delivery mapping $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedProofOfDeliveryMappingStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched proof of delivery mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedProofOfDeliveryMappingStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newBatchedProofOfDeliveryMappingCoordinator(store: BatchedProofOfDeliveryMappingStore, clock: Clock): BatchedProofOfDeliveryMappingCoordinator {
    return BatchedProofOfDeliveryMappingCoordinator(
        store = store,
        validator = defaultBatchedProofOfDeliveryMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
