package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel burn adjustments through their life cycle and keeps a journal of what it did. */
data class FuelBurnAdjustmentService(
    var store: FuelBurnAdjustmentStore,
    var validator: FuelBurnAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel burn adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnAdjustmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnAdjustmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel burn adjustment $code not found")
        val result = engine.run(item)
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

fun newFuelBurnAdjustmentService(store: FuelBurnAdjustmentStore, clock: Clock, flags: Registry): FuelBurnAdjustmentService {
    return FuelBurnAdjustmentService(
        store = store,
        validator = defaultFuelBurnAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnAdjustmentEngine(flags),
    )
}
