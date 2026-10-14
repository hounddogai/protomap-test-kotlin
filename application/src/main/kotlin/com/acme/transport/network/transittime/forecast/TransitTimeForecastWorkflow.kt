package com.acme.transport.network.transittime.forecast

import com.acme.transport.network.clock.Clock

/** Moves transit time forecasts through their life cycle and keeps a journal of what it did. */
data class TransitTimeForecastManager(
    var store: TransitTimeForecastStore,
    var validator: TransitTimeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("transit time forecast $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeForecastStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeForecastStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("transit time forecast $code not found")
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

fun newTransitTimeForecastManager(store: TransitTimeForecastStore, clock: Clock): TransitTimeForecastManager {
    return TransitTimeForecastManager(
        store = store,
        validator = defaultTransitTimeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeForecastEngine(),
    )
}
