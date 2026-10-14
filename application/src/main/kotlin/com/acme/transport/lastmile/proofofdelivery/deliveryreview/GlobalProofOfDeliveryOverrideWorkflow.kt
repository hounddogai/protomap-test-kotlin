package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves global proof of delivery overrides through their life cycle and keeps a journal of what it did.
 */
data class GlobalProofOfDeliveryOverrideService(
    var store: GlobalProofOfDeliveryOverrideStore,
    var validator: GlobalProofOfDeliveryOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global proof of delivery override $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalProofOfDeliveryOverrideStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global proof of delivery override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalProofOfDeliveryOverrideStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newGlobalProofOfDeliveryOverrideService(store: GlobalProofOfDeliveryOverrideStore, clock: Clock): GlobalProofOfDeliveryOverrideService {
    return GlobalProofOfDeliveryOverrideService(
        store = store,
        validator = defaultGlobalProofOfDeliveryOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
