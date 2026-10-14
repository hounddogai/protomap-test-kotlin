package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves pending proof of delivery digests through their life cycle and keeps a journal of what it did.
 */
data class PendingProofOfDeliveryDigestCoordinator(
    var store: PendingProofOfDeliveryDigestStore,
    var validator: PendingProofOfDeliveryDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending proof of delivery digest $code not found")
        validator.requireValid(item)
        item.moveTo(PendingProofOfDeliveryDigestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending proof of delivery digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingProofOfDeliveryDigestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newPendingProofOfDeliveryDigestCoordinator(store: PendingProofOfDeliveryDigestStore, clock: Clock): PendingProofOfDeliveryDigestCoordinator {
    return PendingProofOfDeliveryDigestCoordinator(
        store = store,
        validator = defaultPendingProofOfDeliveryDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
