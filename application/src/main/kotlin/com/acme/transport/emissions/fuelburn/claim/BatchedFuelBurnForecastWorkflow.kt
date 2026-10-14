package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched fuel burn forecasts through their life cycle and keeps a journal of what it did. */
data class BatchedFuelBurnForecastDesk(
    var store: BatchedFuelBurnForecastStore,
    var validator: BatchedFuelBurnForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedFuelBurnForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched fuel burn forecast $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedFuelBurnForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched fuel burn forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedFuelBurnForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched fuel burn forecast $code not found")
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

fun newBatchedFuelBurnForecastDesk(store: BatchedFuelBurnForecastStore, clock: Clock, flags: Registry): BatchedFuelBurnForecastDesk {
    return BatchedFuelBurnForecastDesk(
        store = store,
        validator = defaultBatchedFuelBurnForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedFuelBurnForecastEngine(flags),
    )
}
