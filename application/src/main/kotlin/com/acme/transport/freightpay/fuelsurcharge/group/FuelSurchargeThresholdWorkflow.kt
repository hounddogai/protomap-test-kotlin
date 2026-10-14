package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel surcharge thresholds through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeThresholdService(
    var store: FuelSurchargeThresholdStore,
    var validator: FuelSurchargeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel surcharge threshold $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge threshold $code not found")
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

fun newFuelSurchargeThresholdService(store: FuelSurchargeThresholdStore, clock: Clock, flags: Registry): FuelSurchargeThresholdService {
    return FuelSurchargeThresholdService(
        store = store,
        validator = defaultFuelSurchargeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeThresholdEngine(flags),
    )
}
