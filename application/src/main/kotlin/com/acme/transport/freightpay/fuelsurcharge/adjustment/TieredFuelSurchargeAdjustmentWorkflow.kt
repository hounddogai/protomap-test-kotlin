package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves tiered fuel surcharge adjustments through their life cycle and keeps a journal of what it did.
 */
data class TieredFuelSurchargeAdjustmentManager(
    var store: TieredFuelSurchargeAdjustmentStore,
    var validator: TieredFuelSurchargeAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredFuelSurchargeAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFuelSurchargeAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFuelSurchargeAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered fuel surcharge adjustment $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newTieredFuelSurchargeAdjustmentManager(store: TieredFuelSurchargeAdjustmentStore, clock: Clock, flags: Registry): TieredFuelSurchargeAdjustmentManager {
    return TieredFuelSurchargeAdjustmentManager(
        store = store,
        validator = defaultTieredFuelSurchargeAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredFuelSurchargeAdjustmentEngine(flags),
    )
}
