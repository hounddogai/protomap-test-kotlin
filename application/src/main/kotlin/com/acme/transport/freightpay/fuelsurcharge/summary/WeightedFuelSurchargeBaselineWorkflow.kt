package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves weighted fuel surcharge baselines through their life cycle and keeps a journal of what it did.
 */
data class WeightedFuelSurchargeBaselineDesk(
    var store: WeightedFuelSurchargeBaselineStore,
    var validator: WeightedFuelSurchargeBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedFuelSurchargeBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted fuel surcharge baseline $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedFuelSurchargeBaselineStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted fuel surcharge baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedFuelSurchargeBaselineStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted fuel surcharge baseline $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newWeightedFuelSurchargeBaselineDesk(store: WeightedFuelSurchargeBaselineStore, clock: Clock, flags: Registry): WeightedFuelSurchargeBaselineDesk {
    return WeightedFuelSurchargeBaselineDesk(
        store = store,
        validator = defaultWeightedFuelSurchargeBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedFuelSurchargeBaselineEngine(flags),
    )
}
