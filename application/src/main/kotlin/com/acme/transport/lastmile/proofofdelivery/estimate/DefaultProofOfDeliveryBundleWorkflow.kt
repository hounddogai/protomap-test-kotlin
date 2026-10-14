package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves default proof of delivery bundles through their life cycle and keeps a journal of what it did.
 */
data class DefaultProofOfDeliveryBundleCoordinator(
    var store: DefaultProofOfDeliveryBundleStore,
    var validator: DefaultProofOfDeliveryBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default proof of delivery bundle $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultProofOfDeliveryBundleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default proof of delivery bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultProofOfDeliveryBundleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newDefaultProofOfDeliveryBundleCoordinator(store: DefaultProofOfDeliveryBundleStore, clock: Clock): DefaultProofOfDeliveryBundleCoordinator {
    return DefaultProofOfDeliveryBundleCoordinator(
        store = store,
        validator = defaultDefaultProofOfDeliveryBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
