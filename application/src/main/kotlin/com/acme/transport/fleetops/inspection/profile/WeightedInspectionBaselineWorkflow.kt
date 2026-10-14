package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves weighted inspection baselines through their life cycle and keeps a journal of what it did. */
data class WeightedInspectionBaselineDesk(
    var store: WeightedInspectionBaselineStore,
    var validator: WeightedInspectionBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedInspectionBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted inspection baseline $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedInspectionBaselineStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted inspection baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedInspectionBaselineStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted inspection baseline $code not found")
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

fun newWeightedInspectionBaselineDesk(store: WeightedInspectionBaselineStore, clock: Clock): WeightedInspectionBaselineDesk {
    return WeightedInspectionBaselineDesk(
        store = store,
        validator = defaultWeightedInspectionBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedInspectionBaselineEngine(),
    )
}
