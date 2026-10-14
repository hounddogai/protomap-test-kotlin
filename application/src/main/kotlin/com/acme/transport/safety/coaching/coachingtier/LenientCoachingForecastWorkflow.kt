package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves lenient coaching forecasts through their life cycle and keeps a journal of what it did. */
data class LenientCoachingForecastCoordinator(
    var store: LenientCoachingForecastStore,
    var validator: LenientCoachingForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCoachingForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient coaching forecast $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCoachingForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient coaching forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCoachingForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient coaching forecast $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientCoachingForecastCoordinator(store: LenientCoachingForecastStore, clock: Clock, flags: Registry): LenientCoachingForecastCoordinator {
    return LenientCoachingForecastCoordinator(
        store = store,
        validator = defaultLenientCoachingForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCoachingForecastEngine(flags),
    )
}
