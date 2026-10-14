package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal fuel burn groups through their life cycle and keeps a journal of what it did. */
data class SeasonalFuelBurnGroupDesk(
    var store: SeasonalFuelBurnGroupStore,
    var validator: SeasonalFuelBurnGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalFuelBurnGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal fuel burn group $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalFuelBurnGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal fuel burn group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalFuelBurnGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal fuel burn group $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalFuelBurnGroupDesk(store: SeasonalFuelBurnGroupStore, clock: Clock, flags: Registry): SeasonalFuelBurnGroupDesk {
    return SeasonalFuelBurnGroupDesk(
        store = store,
        validator = defaultSeasonalFuelBurnGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalFuelBurnGroupEngine(flags),
    )
}
