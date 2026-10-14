package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.clock.Clock

/**
 * Moves seasonal route offset thresholds through their life cycle and keeps a journal of what it did.
 */
data class SeasonalRouteOffsetThresholdService(
    var store: SeasonalRouteOffsetThresholdStore,
    var validator: SeasonalRouteOffsetThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalRouteOffsetThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal route offset threshold $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRouteOffsetThresholdStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal route offset threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRouteOffsetThresholdStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal route offset threshold $code not found")
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

fun newSeasonalRouteOffsetThresholdService(store: SeasonalRouteOffsetThresholdStore, clock: Clock): SeasonalRouteOffsetThresholdService {
    return SeasonalRouteOffsetThresholdService(
        store = store,
        validator = defaultSeasonalRouteOffsetThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalRouteOffsetThresholdEngine(),
    )
}
