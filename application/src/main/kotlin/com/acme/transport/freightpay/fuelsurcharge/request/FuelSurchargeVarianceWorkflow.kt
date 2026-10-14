package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge variances through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeVarianceDesk(
    var store: FuelSurchargeVarianceStore,
    var validator: FuelSurchargeVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel surcharge variance $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeVarianceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeVarianceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge variance $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newFuelSurchargeVarianceDesk(store: FuelSurchargeVarianceStore, clock: Clock): FuelSurchargeVarianceDesk {
    return FuelSurchargeVarianceDesk(
        store = store,
        validator = defaultFuelSurchargeVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeVarianceEngine(),
    )
}
