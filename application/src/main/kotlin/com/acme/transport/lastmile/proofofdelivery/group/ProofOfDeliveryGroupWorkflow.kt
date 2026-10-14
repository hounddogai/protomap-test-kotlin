package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery groups through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryGroupDesk(
    var store: ProofOfDeliveryGroupStore,
    var validator: ProofOfDeliveryGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ProofOfDeliveryGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("proof of delivery group $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryGroupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryGroupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("proof of delivery group $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newProofOfDeliveryGroupDesk(store: ProofOfDeliveryGroupStore, clock: Clock): ProofOfDeliveryGroupDesk {
    return ProofOfDeliveryGroupDesk(
        store = store,
        validator = defaultProofOfDeliveryGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultProofOfDeliveryGroupEngine(),
    )
}
