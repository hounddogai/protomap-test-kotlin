package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves global fuel surcharge thresholds through their life cycle and keeps a journal of what it did.
 */
data class GlobalFuelSurchargeThresholdWorkflow(
    var store: GlobalFuelSurchargeThresholdStore,
    var validator: GlobalFuelSurchargeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalFuelSurchargeThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global fuel surcharge threshold $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalFuelSurchargeThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global fuel surcharge threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalFuelSurchargeThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global fuel surcharge threshold $code not found")
        val result = engine.evaluate(item)
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

fun newGlobalFuelSurchargeThresholdWorkflow(store: GlobalFuelSurchargeThresholdStore, clock: Clock, flags: Registry): GlobalFuelSurchargeThresholdWorkflow {
    return GlobalFuelSurchargeThresholdWorkflow(
        store = store,
        validator = defaultGlobalFuelSurchargeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalFuelSurchargeThresholdEngine(flags),
    )
}
