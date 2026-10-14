package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.fleetops.clock.Clock

/** Moves weighted inspection plans through their life cycle and keeps a journal of what it did. */
data class WeightedInspectionPlanService(
    var store: WeightedInspectionPlanStore,
    var validator: WeightedInspectionPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedInspectionPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted inspection plan $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedInspectionPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted inspection plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedInspectionPlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted inspection plan $code not found")
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

fun newWeightedInspectionPlanService(store: WeightedInspectionPlanStore, clock: Clock): WeightedInspectionPlanService {
    return WeightedInspectionPlanService(
        store = store,
        validator = defaultWeightedInspectionPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedInspectionPlanEngine(),
    )
}
