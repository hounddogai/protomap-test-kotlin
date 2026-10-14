package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel burn overrides through their life cycle and keeps a journal of what it did. */
data class FuelBurnOverrideWorkflow(
    var store: FuelBurnOverrideStore,
    var validator: FuelBurnOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel burn override $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel burn override $code not found")
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

fun newFuelBurnOverrideWorkflow(store: FuelBurnOverrideStore, clock: Clock, flags: Registry): FuelBurnOverrideWorkflow {
    return FuelBurnOverrideWorkflow(
        store = store,
        validator = defaultFuelBurnOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnOverrideEngine(flags),
    )
}
