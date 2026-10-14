package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery rules through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryRuleManager(
    var store: ProofOfDeliveryRuleStore,
    var validator: ProofOfDeliveryRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("proof of delivery rule $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newProofOfDeliveryRuleManager(store: ProofOfDeliveryRuleStore, clock: Clock): ProofOfDeliveryRuleManager {
    return ProofOfDeliveryRuleManager(
        store = store,
        validator = defaultProofOfDeliveryRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
