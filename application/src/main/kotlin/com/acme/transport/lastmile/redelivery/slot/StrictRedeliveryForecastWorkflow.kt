package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.lastmile.clock.Clock

/** Moves strict redelivery forecasts through their life cycle and keeps a journal of what it did. */
data class StrictRedeliveryForecastCoordinator(
    var store: StrictRedeliveryForecastStore,
    var validator: StrictRedeliveryForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictRedeliveryForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict redelivery forecast $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRedeliveryForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict redelivery forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRedeliveryForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict redelivery forecast $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newStrictRedeliveryForecastCoordinator(store: StrictRedeliveryForecastStore, clock: Clock): StrictRedeliveryForecastCoordinator {
    return StrictRedeliveryForecastCoordinator(
        store = store,
        validator = defaultStrictRedeliveryForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictRedeliveryForecastEngine(),
    )
}
