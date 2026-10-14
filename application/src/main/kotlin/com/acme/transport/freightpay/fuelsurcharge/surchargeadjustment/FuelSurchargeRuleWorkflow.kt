package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge rules through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeRuleDesk(
    var store: FuelSurchargeRuleStore,
    var validator: FuelSurchargeRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel surcharge rule $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFuelSurchargeRuleDesk(store: FuelSurchargeRuleStore, clock: Clock): FuelSurchargeRuleDesk {
    return FuelSurchargeRuleDesk(
        store = store,
        validator = defaultFuelSurchargeRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
