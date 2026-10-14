package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.emissions.clock.Clock

/**
 * Moves weighted carbon report records through their life cycle and keeps a journal of what it did.
 */
data class WeightedCarbonReportRecordCoordinator(
    var store: WeightedCarbonReportRecordStore,
    var validator: WeightedCarbonReportRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedCarbonReportRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted carbon report record $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedCarbonReportRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted carbon report record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedCarbonReportRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted carbon report record $code not found")
        val result = engine.compute(item)
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

fun newWeightedCarbonReportRecordCoordinator(store: WeightedCarbonReportRecordStore, clock: Clock): WeightedCarbonReportRecordCoordinator {
    return WeightedCarbonReportRecordCoordinator(
        store = store,
        validator = defaultWeightedCarbonReportRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedCarbonReportRecordEngine(),
    )
}
