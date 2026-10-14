package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves carbon report variances through their life cycle and keeps a journal of what it did. */
data class CarbonReportVarianceManager(
    var store: CarbonReportVarianceStore,
    var validator: CarbonReportVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("carbon report variance $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportVarianceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportVarianceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("carbon report variance $code not found")
        val result = engine.run(item)
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

fun newCarbonReportVarianceManager(store: CarbonReportVarianceStore, clock: Clock, flags: Registry): CarbonReportVarianceManager {
    return CarbonReportVarianceManager(
        store = store,
        validator = defaultCarbonReportVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportVarianceEngine(flags),
    )
}
