package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery digests through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryDigestService(
    var store: ProofOfDeliveryDigestStore,
    var validator: ProofOfDeliveryDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ProofOfDeliveryDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("proof of delivery digest $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryDigestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryDigestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("proof of delivery digest $code not found")
        val result = engine.compute(item)
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

fun newProofOfDeliveryDigestService(store: ProofOfDeliveryDigestStore, clock: Clock, flags: Registry): ProofOfDeliveryDigestService {
    return ProofOfDeliveryDigestService(
        store = store,
        validator = defaultProofOfDeliveryDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultProofOfDeliveryDigestEngine(flags),
    )
}
