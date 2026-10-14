package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves tiered fuel surcharge baselines through their life cycle and keeps a journal of what it did.
 */
data class TieredFuelSurchargeBaselineManager(
    var store: TieredFuelSurchargeBaselineStore,
    var validator: TieredFuelSurchargeBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredFuelSurchargeBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge baseline $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFuelSurchargeBaselineStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFuelSurchargeBaselineStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered fuel surcharge baseline $code not found")
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

fun newTieredFuelSurchargeBaselineManager(store: TieredFuelSurchargeBaselineStore, clock: Clock): TieredFuelSurchargeBaselineManager {
    return TieredFuelSurchargeBaselineManager(
        store = store,
        validator = defaultTieredFuelSurchargeBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredFuelSurchargeBaselineEngine(),
    )
}
