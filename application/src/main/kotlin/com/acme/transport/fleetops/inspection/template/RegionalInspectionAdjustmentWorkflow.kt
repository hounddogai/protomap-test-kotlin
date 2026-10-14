package com.acme.transport.fleetops.inspection.template

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves regional inspection adjustments through their life cycle and keeps a journal of what it did.
 */
data class RegionalInspectionAdjustmentWorkflow(
    var store: RegionalInspectionAdjustmentStore,
    var validator: RegionalInspectionAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalInspectionAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional inspection adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalInspectionAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional inspection adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalInspectionAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional inspection adjustment $code not found")
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

fun newRegionalInspectionAdjustmentWorkflow(store: RegionalInspectionAdjustmentStore, clock: Clock, flags: Registry): RegionalInspectionAdjustmentWorkflow {
    return RegionalInspectionAdjustmentWorkflow(
        store = store,
        validator = defaultRegionalInspectionAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalInspectionAdjustmentEngine(flags),
    )
}
