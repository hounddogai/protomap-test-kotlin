package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.network.clock.Clock

/** Moves seasonal transit time metrics through their life cycle and keeps a journal of what it did. */
data class SeasonalTransitTimeMetricService(
    var store: SeasonalTransitTimeMetricStore,
    var validator: SeasonalTransitTimeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal transit time metric $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTransitTimeMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal transit time metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTransitTimeMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newSeasonalTransitTimeMetricService(store: SeasonalTransitTimeMetricStore, clock: Clock): SeasonalTransitTimeMetricService {
    return SeasonalTransitTimeMetricService(
        store = store,
        validator = defaultSeasonalTransitTimeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
