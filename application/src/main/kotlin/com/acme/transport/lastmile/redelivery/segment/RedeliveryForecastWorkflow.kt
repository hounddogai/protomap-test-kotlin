package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery forecasts through their life cycle and keeps a journal of what it did. */
data class RedeliveryForecastManager(
    var store: RedeliveryForecastStore,
    var validator: RedeliveryForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("redelivery forecast $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("redelivery forecast $code not found")
        val result = engine.evaluate(item)
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

fun newRedeliveryForecastManager(store: RedeliveryForecastStore, clock: Clock, flags: Registry): RedeliveryForecastManager {
    return RedeliveryForecastManager(
        store = store,
        validator = defaultRedeliveryForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryForecastEngine(flags),
    )
}
