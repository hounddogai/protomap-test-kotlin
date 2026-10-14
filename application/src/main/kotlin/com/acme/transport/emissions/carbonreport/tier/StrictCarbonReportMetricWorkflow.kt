package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict carbon report metrics through their life cycle and keeps a journal of what it did. */
data class StrictCarbonReportMetricService(
    var store: StrictCarbonReportMetricStore,
    var validator: StrictCarbonReportMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictCarbonReportMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict carbon report metric $code not found")
        validator.requireValid(item)
        item.moveTo(StrictCarbonReportMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict carbon report metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictCarbonReportMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict carbon report metric $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newStrictCarbonReportMetricService(store: StrictCarbonReportMetricStore, clock: Clock, flags: Registry): StrictCarbonReportMetricService {
    return StrictCarbonReportMetricService(
        store = store,
        validator = defaultStrictCarbonReportMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictCarbonReportMetricEngine(flags),
    )
}
