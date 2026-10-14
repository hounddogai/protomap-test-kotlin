package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.emissions.clock.Clock

/**
 * Moves weighted carbon report estimates through their life cycle and keeps a journal of what it did.
 */
data class WeightedCarbonReportEstimateManager(
    var store: WeightedCarbonReportEstimateStore,
    var validator: WeightedCarbonReportEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedCarbonReportEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted carbon report estimate $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedCarbonReportEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted carbon report estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedCarbonReportEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted carbon report estimate $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newWeightedCarbonReportEstimateManager(store: WeightedCarbonReportEstimateStore, clock: Clock): WeightedCarbonReportEstimateManager {
    return WeightedCarbonReportEstimateManager(
        store = store,
        validator = defaultWeightedCarbonReportEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedCarbonReportEstimateEngine(),
    )
}
