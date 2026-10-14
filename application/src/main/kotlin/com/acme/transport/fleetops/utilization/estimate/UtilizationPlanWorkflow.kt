package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization plans through their life cycle and keeps a journal of what it did. */
data class UtilizationPlanService(
    var store: UtilizationPlanStore,
    var validator: UtilizationPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("utilization plan $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationPlanStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationPlanStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newUtilizationPlanService(store: UtilizationPlanStore, clock: Clock): UtilizationPlanService {
    return UtilizationPlanService(
        store = store,
        validator = defaultUtilizationPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
