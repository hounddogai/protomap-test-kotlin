package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel burn claims through their life cycle and keeps a journal of what it did. */
data class FuelBurnClaimDesk(
    var store: FuelBurnClaimStore,
    var validator: FuelBurnClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel burn claim $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fuel burn claim $code not found")
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

fun newFuelBurnClaimDesk(store: FuelBurnClaimStore, clock: Clock, flags: Registry): FuelBurnClaimDesk {
    return FuelBurnClaimDesk(
        store = store,
        validator = defaultFuelBurnClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnClaimEngine(flags),
    )
}
