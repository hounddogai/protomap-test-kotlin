package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves tiered proof of delivery scores through their life cycle and keeps a journal of what it did.
 */
data class TieredProofOfDeliveryScoreDesk(
    var store: TieredProofOfDeliveryScoreStore,
    var validator: TieredProofOfDeliveryScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredProofOfDeliveryScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered proof of delivery score $code not found")
        validator.requireValid(item)
        item.moveTo(TieredProofOfDeliveryScoreStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered proof of delivery score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredProofOfDeliveryScoreStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered proof of delivery score $code not found")
        val result = engine.compute(item)
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

fun newTieredProofOfDeliveryScoreDesk(store: TieredProofOfDeliveryScoreStore, clock: Clock): TieredProofOfDeliveryScoreDesk {
    return TieredProofOfDeliveryScoreDesk(
        store = store,
        validator = defaultTieredProofOfDeliveryScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredProofOfDeliveryScoreEngine(),
    )
}
