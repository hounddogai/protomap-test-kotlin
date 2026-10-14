package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn rollups through their life cycle and keeps a journal of what it did. */
data class FuelBurnRollupDesk(
    var store: FuelBurnRollupStore,
    var validator: FuelBurnRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel burn rollup $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel burn rollup $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newFuelBurnRollupDesk(store: FuelBurnRollupStore, clock: Clock): FuelBurnRollupDesk {
    return FuelBurnRollupDesk(
        store = store,
        validator = defaultFuelBurnRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnRollupEngine(),
    )
}
