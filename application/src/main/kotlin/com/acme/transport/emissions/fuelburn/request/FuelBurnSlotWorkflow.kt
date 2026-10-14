package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn slots through their life cycle and keeps a journal of what it did. */
data class FuelBurnSlotDesk(
    var store: FuelBurnSlotStore,
    var validator: FuelBurnSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel burn slot $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnSlotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnSlotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fuel burn slot $code not found")
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

fun newFuelBurnSlotDesk(store: FuelBurnSlotStore, clock: Clock): FuelBurnSlotDesk {
    return FuelBurnSlotDesk(
        store = store,
        validator = defaultFuelBurnSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnSlotEngine(),
    )
}
