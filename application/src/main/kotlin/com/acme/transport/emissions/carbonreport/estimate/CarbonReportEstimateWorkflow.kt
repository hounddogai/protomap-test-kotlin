package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves carbon report estimates through their life cycle and keeps a journal of what it did. */
data class CarbonReportEstimateCoordinator(
    var store: CarbonReportEstimateStore,
    var validator: CarbonReportEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("carbon report estimate $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("carbon report estimate $code not found")
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

fun newCarbonReportEstimateCoordinator(store: CarbonReportEstimateStore, clock: Clock, flags: Registry): CarbonReportEstimateCoordinator {
    return CarbonReportEstimateCoordinator(
        store = store,
        validator = defaultCarbonReportEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportEstimateEngine(flags),
    )
}
