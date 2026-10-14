package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel surcharge preferences through their life cycle and keeps a journal of what it did. */
data class FuelSurchargePreferenceWorkflow(
    var store: FuelSurchargePreferenceStore,
    var validator: FuelSurchargePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargePreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel surcharge preference $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargePreferenceStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargePreferenceStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge preference $code not found")
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

fun newFuelSurchargePreferenceWorkflow(store: FuelSurchargePreferenceStore, clock: Clock, flags: Registry): FuelSurchargePreferenceWorkflow {
    return FuelSurchargePreferenceWorkflow(
        store = store,
        validator = defaultFuelSurchargePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargePreferenceEngine(flags),
    )
}
