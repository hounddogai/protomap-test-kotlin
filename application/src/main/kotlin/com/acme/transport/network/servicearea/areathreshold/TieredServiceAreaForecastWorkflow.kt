package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.network.clock.Clock

/** Moves tiered service area forecasts through their life cycle and keeps a journal of what it did. */
data class TieredServiceAreaForecastManager(
    var store: TieredServiceAreaForecastStore,
    var validator: TieredServiceAreaForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredServiceAreaForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered service area forecast $code not found")
        validator.requireValid(item)
        item.moveTo(TieredServiceAreaForecastStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered service area forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredServiceAreaForecastStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tiered service area forecast $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newTieredServiceAreaForecastManager(store: TieredServiceAreaForecastStore, clock: Clock): TieredServiceAreaForecastManager {
    return TieredServiceAreaForecastManager(
        store = store,
        validator = defaultTieredServiceAreaForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredServiceAreaForecastEngine(),
    )
}
