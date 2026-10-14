package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves proof of delivery schedules through their life cycle and keeps a journal of what it did. */
data class ProofOfDeliveryScheduleManager(
    var store: ProofOfDeliveryScheduleStore,
    var validator: ProofOfDeliveryScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ProofOfDeliveryScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("proof of delivery schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ProofOfDeliveryScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("proof of delivery schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ProofOfDeliveryScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("proof of delivery schedule $code not found")
        val result = engine.run(item)
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

fun newProofOfDeliveryScheduleManager(store: ProofOfDeliveryScheduleStore, clock: Clock, flags: Registry): ProofOfDeliveryScheduleManager {
    return ProofOfDeliveryScheduleManager(
        store = store,
        validator = defaultProofOfDeliveryScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultProofOfDeliveryScheduleEngine(flags),
    )
}
