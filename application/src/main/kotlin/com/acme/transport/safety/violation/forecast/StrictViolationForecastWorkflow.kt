package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves strict violation forecasts through their life cycle and keeps a journal of what it did. */
data class StrictViolationForecastCoordinator(
    var store: StrictViolationForecastStore,
    var validator: StrictViolationForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictViolationForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict violation forecast $code not found")
        validator.requireValid(item)
        item.moveTo(StrictViolationForecastStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict violation forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictViolationForecastStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict violation forecast $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newStrictViolationForecastCoordinator(store: StrictViolationForecastStore, clock: Clock, flags: Registry): StrictViolationForecastCoordinator {
    return StrictViolationForecastCoordinator(
        store = store,
        validator = defaultStrictViolationForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictViolationForecastEngine(flags),
    )
}
