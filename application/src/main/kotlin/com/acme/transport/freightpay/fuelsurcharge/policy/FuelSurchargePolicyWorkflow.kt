package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge policies through their life cycle and keeps a journal of what it did. */
data class FuelSurchargePolicyManager(
    var store: FuelSurchargePolicyStore,
    var validator: FuelSurchargePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fuel surcharge policy $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargePolicyStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargePolicyStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge policy $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newFuelSurchargePolicyManager(store: FuelSurchargePolicyStore, clock: Clock): FuelSurchargePolicyManager {
    return FuelSurchargePolicyManager(
        store = store,
        validator = defaultFuelSurchargePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargePolicyEngine(),
    )
}
