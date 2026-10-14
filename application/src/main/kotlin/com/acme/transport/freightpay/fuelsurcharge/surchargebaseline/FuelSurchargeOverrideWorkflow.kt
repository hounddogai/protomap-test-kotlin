package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel surcharge overrides through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeOverrideCoordinator(
    var store: FuelSurchargeOverrideStore,
    var validator: FuelSurchargeOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel surcharge override $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge override $code not found")
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

fun newFuelSurchargeOverrideCoordinator(store: FuelSurchargeOverrideStore, clock: Clock, flags: Registry): FuelSurchargeOverrideCoordinator {
    return FuelSurchargeOverrideCoordinator(
        store = store,
        validator = defaultFuelSurchargeOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeOverrideEngine(flags),
    )
}
