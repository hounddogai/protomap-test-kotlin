package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves rolling proof of delivery quotas through their life cycle and keeps a journal of what it did.
 */
data class RollingProofOfDeliveryQuotaManager(
    var store: RollingProofOfDeliveryQuotaStore,
    var validator: RollingProofOfDeliveryQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingProofOfDeliveryQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling proof of delivery quota $code not found")
        validator.requireValid(item)
        item.moveTo(RollingProofOfDeliveryQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling proof of delivery quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingProofOfDeliveryQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling proof of delivery quota $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newRollingProofOfDeliveryQuotaManager(store: RollingProofOfDeliveryQuotaStore, clock: Clock): RollingProofOfDeliveryQuotaManager {
    return RollingProofOfDeliveryQuotaManager(
        store = store,
        validator = defaultRollingProofOfDeliveryQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingProofOfDeliveryQuotaEngine(),
    )
}
