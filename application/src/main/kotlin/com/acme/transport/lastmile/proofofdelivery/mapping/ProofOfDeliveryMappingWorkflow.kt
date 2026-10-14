package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery mappings through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryMappingCoordinator(
    var store: ProofOfDeliveryMappingStore,
    var validator: ProofOfDeliveryMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ProofOfDeliveryMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("proof of delivery mapping $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("proof of delivery mapping $code not found")
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

fun newProofOfDeliveryMappingCoordinator(store: ProofOfDeliveryMappingStore, clock: Clock): ProofOfDeliveryMappingCoordinator {
    return ProofOfDeliveryMappingCoordinator(
        store = store,
        validator = defaultProofOfDeliveryMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultProofOfDeliveryMappingEngine(),
    )
}
