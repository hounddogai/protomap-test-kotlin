package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves regional carbon report adjustments through their life cycle and keeps a journal of what it did.
 */
data class RegionalCarbonReportAdjustmentService(
    var store: RegionalCarbonReportAdjustmentStore,
    var validator: RegionalCarbonReportAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCarbonReportAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional carbon report adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCarbonReportAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional carbon report adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCarbonReportAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional carbon report adjustment $code not found")
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

fun newRegionalCarbonReportAdjustmentService(store: RegionalCarbonReportAdjustmentStore, clock: Clock, flags: Registry): RegionalCarbonReportAdjustmentService {
    return RegionalCarbonReportAdjustmentService(
        store = store,
        validator = defaultRegionalCarbonReportAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCarbonReportAdjustmentEngine(flags),
    )
}
