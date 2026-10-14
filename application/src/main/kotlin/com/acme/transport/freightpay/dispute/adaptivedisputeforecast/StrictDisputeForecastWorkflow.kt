package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict dispute forecasts through their life cycle and keeps a journal of what it did. */
data class StrictDisputeForecastManager(
    var store: StrictDisputeForecastStore,
    var validator: StrictDisputeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictDisputeForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict dispute forecast $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDisputeForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dispute forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDisputeForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict dispute forecast $code not found")
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

fun newStrictDisputeForecastManager(store: StrictDisputeForecastStore, clock: Clock, flags: Registry): StrictDisputeForecastManager {
    return StrictDisputeForecastManager(
        store = store,
        validator = defaultStrictDisputeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictDisputeForecastEngine(flags),
    )
}
