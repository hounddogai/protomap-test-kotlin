package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.lastmile.clock.Clock

/** Moves weighted drop forecasts through their life cycle and keeps a journal of what it did. */
data class WeightedDropForecastManager(
    var store: WeightedDropForecastStore,
    var validator: WeightedDropForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDropForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted drop forecast $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDropForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted drop forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDropForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted drop forecast $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newWeightedDropForecastManager(store: WeightedDropForecastStore, clock: Clock): WeightedDropForecastManager {
    return WeightedDropForecastManager(
        store = store,
        validator = defaultWeightedDropForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDropForecastEngine(),
    )
}
