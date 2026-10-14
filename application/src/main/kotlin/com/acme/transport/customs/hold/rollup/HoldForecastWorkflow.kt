package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.clock.Clock

/** Moves hold forecasts through their life cycle and keeps a journal of what it did. */
data class HoldForecastCoordinator(
    var store: HoldForecastStore,
    var validator: HoldForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hold forecast $code not found")
        validator.requireValid(item)
        item.moveTo(HoldForecastStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hold forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldForecastStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newHoldForecastCoordinator(store: HoldForecastStore, clock: Clock): HoldForecastCoordinator {
    return HoldForecastCoordinator(
        store = store,
        validator = defaultHoldForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
