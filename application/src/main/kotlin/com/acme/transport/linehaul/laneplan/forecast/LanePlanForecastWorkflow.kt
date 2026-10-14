package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan forecasts through their life cycle and keeps a journal of what it did. */
data class LanePlanForecastCoordinator(
    var store: LanePlanForecastStore,
    var validator: LanePlanForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan forecast $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLanePlanForecastCoordinator(store: LanePlanForecastStore, clock: Clock): LanePlanForecastCoordinator {
    return LanePlanForecastCoordinator(
        store = store,
        validator = defaultLanePlanForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
