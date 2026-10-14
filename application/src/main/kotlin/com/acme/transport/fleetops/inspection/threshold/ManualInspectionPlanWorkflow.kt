package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual inspection plans through their life cycle and keeps a journal of what it did. */
data class ManualInspectionPlanService(
    var store: ManualInspectionPlanStore,
    var validator: ManualInspectionPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualInspectionPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual inspection plan $code not found")
        validator.requireValid(item)
        item.moveTo(ManualInspectionPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual inspection plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualInspectionPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual inspection plan $code not found")
        val result = engine.price(item)
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

fun newManualInspectionPlanService(store: ManualInspectionPlanStore, clock: Clock, flags: Registry): ManualInspectionPlanService {
    return ManualInspectionPlanService(
        store = store,
        validator = defaultManualInspectionPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualInspectionPlanEngine(flags),
    )
}
