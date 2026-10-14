package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.clock.Clock

/**
 * Moves global carbon report adjustments through their life cycle and keeps a journal of what it did.
 */
data class GlobalCarbonReportAdjustmentWorkflow(
    var store: GlobalCarbonReportAdjustmentStore,
    var validator: GlobalCarbonReportAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global carbon report adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCarbonReportAdjustmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global carbon report adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCarbonReportAdjustmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newGlobalCarbonReportAdjustmentWorkflow(store: GlobalCarbonReportAdjustmentStore, clock: Clock): GlobalCarbonReportAdjustmentWorkflow {
    return GlobalCarbonReportAdjustmentWorkflow(
        store = store,
        validator = defaultGlobalCarbonReportAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
