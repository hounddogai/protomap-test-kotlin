package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection adjustments through their life cycle and keeps a journal of what it did. */
data class InspectionAdjustmentCoordinator(
    var store: InspectionAdjustmentStore,
    var validator: InspectionAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection adjustment $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newInspectionAdjustmentCoordinator(store: InspectionAdjustmentStore, clock: Clock, flags: Registry): InspectionAdjustmentCoordinator {
    return InspectionAdjustmentCoordinator(
        store = store,
        validator = defaultInspectionAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionAdjustmentEngine(flags),
    )
}
