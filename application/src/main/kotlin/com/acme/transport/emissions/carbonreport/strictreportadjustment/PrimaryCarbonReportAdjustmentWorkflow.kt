package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves primary carbon report adjustments through their life cycle and keeps a journal of what it did.
 */
data class PrimaryCarbonReportAdjustmentWorkflow(
    var store: PrimaryCarbonReportAdjustmentStore,
    var validator: PrimaryCarbonReportAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryCarbonReportAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary carbon report adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCarbonReportAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary carbon report adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCarbonReportAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary carbon report adjustment $code not found")
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

fun newPrimaryCarbonReportAdjustmentWorkflow(store: PrimaryCarbonReportAdjustmentStore, clock: Clock, flags: Registry): PrimaryCarbonReportAdjustmentWorkflow {
    return PrimaryCarbonReportAdjustmentWorkflow(
        store = store,
        validator = defaultPrimaryCarbonReportAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryCarbonReportAdjustmentEngine(flags),
    )
}
