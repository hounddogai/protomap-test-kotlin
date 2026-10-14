package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves rolling proof of delivery mappings through their life cycle and keeps a journal of what it did.
 */
data class RollingProofOfDeliveryMappingManager(
    var store: RollingProofOfDeliveryMappingStore,
    var validator: RollingProofOfDeliveryMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling proof of delivery mapping $code not found")
        validator.requireValid(item)
        item.moveTo(RollingProofOfDeliveryMappingStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling proof of delivery mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingProofOfDeliveryMappingStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRollingProofOfDeliveryMappingManager(store: RollingProofOfDeliveryMappingStore, clock: Clock): RollingProofOfDeliveryMappingManager {
    return RollingProofOfDeliveryMappingManager(
        store = store,
        validator = defaultRollingProofOfDeliveryMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
