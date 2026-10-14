package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery windows through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryWindowManager(
    var store: ProofOfDeliveryWindowStore,
    var validator: ProofOfDeliveryWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ProofOfDeliveryWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("proof of delivery window $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryWindowStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryWindowStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("proof of delivery window $code not found")
        val result = engine.compute(item)
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

fun newProofOfDeliveryWindowManager(store: ProofOfDeliveryWindowStore, clock: Clock): ProofOfDeliveryWindowManager {
    return ProofOfDeliveryWindowManager(
        store = store,
        validator = defaultProofOfDeliveryWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultProofOfDeliveryWindowEngine(),
    )
}
