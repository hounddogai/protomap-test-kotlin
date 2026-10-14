package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy fuel surcharge windows through their life cycle and keeps a journal of what it did. */
data class LegacyFuelSurchargeWindowCoordinator(
    var store: LegacyFuelSurchargeWindowStore,
    var validator: LegacyFuelSurchargeWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyFuelSurchargeWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy fuel surcharge window $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyFuelSurchargeWindowStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy fuel surcharge window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyFuelSurchargeWindowStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy fuel surcharge window $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newLegacyFuelSurchargeWindowCoordinator(store: LegacyFuelSurchargeWindowStore, clock: Clock, flags: Registry): LegacyFuelSurchargeWindowCoordinator {
    return LegacyFuelSurchargeWindowCoordinator(
        store = store,
        validator = defaultLegacyFuelSurchargeWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyFuelSurchargeWindowEngine(flags),
    )
}
