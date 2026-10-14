package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation forecasts through their life cycle and keeps a journal of what it did. */
data class SortationForecastManager(
    var store: SortationForecastStore,
    var validator: SortationForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("sortation forecast $code not found")
        validator.requireValid(item)
        item.moveTo(SortationForecastStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationForecastStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("sortation forecast $code not found")
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

fun newSortationForecastManager(store: SortationForecastStore, clock: Clock, flags: Registry): SortationForecastManager {
    return SortationForecastManager(
        store = store,
        validator = defaultSortationForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationForecastEngine(flags),
    )
}
