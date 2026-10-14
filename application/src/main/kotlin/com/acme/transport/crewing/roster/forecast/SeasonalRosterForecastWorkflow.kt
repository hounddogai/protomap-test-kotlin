package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.clock.Clock

/** Moves seasonal roster forecasts through their life cycle and keeps a journal of what it did. */
data class SeasonalRosterForecastService(
    var store: SeasonalRosterForecastStore,
    var validator: SeasonalRosterForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal roster forecast $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRosterForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal roster forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRosterForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newSeasonalRosterForecastService(store: SeasonalRosterForecastStore, clock: Clock): SeasonalRosterForecastService {
    return SeasonalRosterForecastService(
        store = store,
        validator = defaultSeasonalRosterForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
