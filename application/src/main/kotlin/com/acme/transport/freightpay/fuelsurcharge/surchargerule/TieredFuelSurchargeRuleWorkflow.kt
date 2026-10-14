package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.freightpay.clock.Clock

/** Moves tiered fuel surcharge rules through their life cycle and keeps a journal of what it did. */
data class TieredFuelSurchargeRuleManager(
    var store: TieredFuelSurchargeRuleStore,
    var validator: TieredFuelSurchargeRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge rule $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFuelSurchargeRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFuelSurchargeRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newTieredFuelSurchargeRuleManager(store: TieredFuelSurchargeRuleStore, clock: Clock): TieredFuelSurchargeRuleManager {
    return TieredFuelSurchargeRuleManager(
        store = store,
        validator = defaultTieredFuelSurchargeRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
