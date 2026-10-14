package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves carbon report adjustments through their life cycle and keeps a journal of what it did. */
data class CarbonReportAdjustmentManager(
    var store: CarbonReportAdjustmentStore,
    var validator: CarbonReportAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("carbon report adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("carbon report adjustment $code not found")
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

fun newCarbonReportAdjustmentManager(store: CarbonReportAdjustmentStore, clock: Clock, flags: Registry): CarbonReportAdjustmentManager {
    return CarbonReportAdjustmentManager(
        store = store,
        validator = defaultCarbonReportAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportAdjustmentEngine(flags),
    )
}
