package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves capped proof of delivery budgets through their life cycle and keeps a journal of what it did.
 */
data class CappedProofOfDeliveryBudgetDesk(
    var store: CappedProofOfDeliveryBudgetStore,
    var validator: CappedProofOfDeliveryBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedProofOfDeliveryBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped proof of delivery budget $code not found")
        validator.requireValid(item)
        item.moveTo(CappedProofOfDeliveryBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped proof of delivery budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedProofOfDeliveryBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped proof of delivery budget $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedProofOfDeliveryBudgetDesk(store: CappedProofOfDeliveryBudgetStore, clock: Clock): CappedProofOfDeliveryBudgetDesk {
    return CappedProofOfDeliveryBudgetDesk(
        store = store,
        validator = defaultCappedProofOfDeliveryBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedProofOfDeliveryBudgetEngine(),
    )
}
