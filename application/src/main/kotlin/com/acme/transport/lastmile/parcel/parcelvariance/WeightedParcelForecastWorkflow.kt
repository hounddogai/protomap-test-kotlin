package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves weighted parcel forecasts through their life cycle and keeps a journal of what it did. */
data class WeightedParcelForecastDesk(
    var store: WeightedParcelForecastStore,
    var validator: WeightedParcelForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedParcelForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted parcel forecast $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedParcelForecastStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted parcel forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedParcelForecastStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted parcel forecast $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newWeightedParcelForecastDesk(store: WeightedParcelForecastStore, clock: Clock, flags: Registry): WeightedParcelForecastDesk {
    return WeightedParcelForecastDesk(
        store = store,
        validator = defaultWeightedParcelForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedParcelForecastEngine(flags),
    )
}
