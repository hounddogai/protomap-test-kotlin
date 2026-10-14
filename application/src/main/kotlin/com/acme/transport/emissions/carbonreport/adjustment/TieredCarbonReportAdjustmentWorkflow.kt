package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.emissions.clock.Clock

/**
 * Moves tiered carbon report adjustments through their life cycle and keeps a journal of what it did.
 */
data class TieredCarbonReportAdjustmentWorkflow(
    var store: TieredCarbonReportAdjustmentStore,
    var validator: TieredCarbonReportAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered carbon report adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCarbonReportAdjustmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered carbon report adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCarbonReportAdjustmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTieredCarbonReportAdjustmentWorkflow(store: TieredCarbonReportAdjustmentStore, clock: Clock): TieredCarbonReportAdjustmentWorkflow {
    return TieredCarbonReportAdjustmentWorkflow(
        store = store,
        validator = defaultTieredCarbonReportAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
