package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves scheduled emission factor metrics through their life cycle and keeps a journal of what it did.
 */
data class ScheduledEmissionFactorMetricService(
    var store: ScheduledEmissionFactorMetricStore,
    var validator: ScheduledEmissionFactorMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledEmissionFactorMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled emission factor metric $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledEmissionFactorMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled emission factor metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledEmissionFactorMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled emission factor metric $code not found")
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

fun newScheduledEmissionFactorMetricService(store: ScheduledEmissionFactorMetricStore, clock: Clock, flags: Registry): ScheduledEmissionFactorMetricService {
    return ScheduledEmissionFactorMetricService(
        store = store,
        validator = defaultScheduledEmissionFactorMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledEmissionFactorMetricEngine(flags),
    )
}
