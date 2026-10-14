package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves legacy proof of delivery windows through their life cycle and keeps a journal of what it did.
 */
data class LegacyProofOfDeliveryWindowService(
    var store: LegacyProofOfDeliveryWindowStore,
    var validator: LegacyProofOfDeliveryWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyProofOfDeliveryWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy proof of delivery window $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyProofOfDeliveryWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy proof of delivery window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyProofOfDeliveryWindowStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy proof of delivery window $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newLegacyProofOfDeliveryWindowService(store: LegacyProofOfDeliveryWindowStore, clock: Clock): LegacyProofOfDeliveryWindowService {
    return LegacyProofOfDeliveryWindowService(
        store = store,
        validator = defaultLegacyProofOfDeliveryWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyProofOfDeliveryWindowEngine(),
    )
}
