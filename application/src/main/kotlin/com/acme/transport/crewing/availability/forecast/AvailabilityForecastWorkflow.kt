package com.acme.transport.crewing.availability.forecast

import com.acme.transport.crewing.clock.Clock

/** Moves availability forecasts through their life cycle and keeps a journal of what it did. */
data class AvailabilityForecastDesk(
    var store: AvailabilityForecastStore,
    var validator: AvailabilityForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("availability forecast $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityForecastStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("availability forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityForecastStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newAvailabilityForecastDesk(store: AvailabilityForecastStore, clock: Clock): AvailabilityForecastDesk {
    return AvailabilityForecastDesk(
        store = store,
        validator = defaultAvailabilityForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
