package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge forecasts through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeForecastCoordinator(
    var store: FuelSurchargeForecastStore,
    var validator: FuelSurchargeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel surcharge forecast $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeForecastStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeForecastStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge forecast $code not found")
        val result = engine.evaluate(item)
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

fun newFuelSurchargeForecastCoordinator(store: FuelSurchargeForecastStore, clock: Clock): FuelSurchargeForecastCoordinator {
    return FuelSurchargeForecastCoordinator(
        store = store,
        validator = defaultFuelSurchargeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeForecastEngine(),
    )
}
