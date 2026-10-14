package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves carbon report metrics through their life cycle and keeps a journal of what it did. */
data class CarbonReportMetricService(
    var store: CarbonReportMetricStore,
    var validator: CarbonReportMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("carbon report metric $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportMetricStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportMetricStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("carbon report metric $code not found")
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

fun newCarbonReportMetricService(store: CarbonReportMetricStore, clock: Clock, flags: Registry): CarbonReportMetricService {
    return CarbonReportMetricService(
        store = store,
        validator = defaultCarbonReportMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportMetricEngine(flags),
    )
}
