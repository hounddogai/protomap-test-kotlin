package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.safety.clock.Clock

/** Moves seasonal coaching metrics through their life cycle and keeps a journal of what it did. */
data class SeasonalCoachingMetricService(
    var store: SeasonalCoachingMetricStore,
    var validator: SeasonalCoachingMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalCoachingMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal coaching metric $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCoachingMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal coaching metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCoachingMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal coaching metric $code not found")
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

fun newSeasonalCoachingMetricService(store: SeasonalCoachingMetricStore, clock: Clock): SeasonalCoachingMetricService {
    return SeasonalCoachingMetricService(
        store = store,
        validator = defaultSeasonalCoachingMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalCoachingMetricEngine(),
    )
}
