package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal ev charge forecasts through their life cycle and keeps a journal of what it did. */
data class SeasonalEvChargeForecastDesk(
    var store: SeasonalEvChargeForecastStore,
    var validator: SeasonalEvChargeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalEvChargeForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal ev charge forecast $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalEvChargeForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal ev charge forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalEvChargeForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal ev charge forecast $code not found")
        val result = engine.price(item)
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

fun newSeasonalEvChargeForecastDesk(store: SeasonalEvChargeForecastStore, clock: Clock, flags: Registry): SeasonalEvChargeForecastDesk {
    return SeasonalEvChargeForecastDesk(
        store = store,
        validator = defaultSeasonalEvChargeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalEvChargeForecastEngine(flags),
    )
}
