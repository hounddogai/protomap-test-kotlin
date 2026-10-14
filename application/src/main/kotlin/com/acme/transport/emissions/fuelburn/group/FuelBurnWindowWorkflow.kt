package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel burn windows through their life cycle and keeps a journal of what it did. */
data class FuelBurnWindowCoordinator(
    var store: FuelBurnWindowStore,
    var validator: FuelBurnWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel burn window $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnWindowStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnWindowStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel burn window $code not found")
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

fun newFuelBurnWindowCoordinator(store: FuelBurnWindowStore, clock: Clock, flags: Registry): FuelBurnWindowCoordinator {
    return FuelBurnWindowCoordinator(
        store = store,
        validator = defaultFuelBurnWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnWindowEngine(flags),
    )
}
