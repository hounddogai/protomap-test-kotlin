package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute forecasts through their life cycle and keeps a journal of what it did. */
data class DisputeForecastManager(
    var store: DisputeForecastStore,
    var validator: DisputeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dispute forecast $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dispute forecast $code not found")
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

fun newDisputeForecastManager(store: DisputeForecastStore, clock: Clock, flags: Registry): DisputeForecastManager {
    return DisputeForecastManager(
        store = store,
        validator = defaultDisputeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeForecastEngine(flags),
    )
}
