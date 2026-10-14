package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection estimates through their life cycle and keeps a journal of what it did. */
data class InspectionEstimateWorkflow(
    var store: InspectionEstimateStore,
    var validator: InspectionEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection estimate $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionEstimateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionEstimateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newInspectionEstimateWorkflow(store: InspectionEstimateStore, clock: Clock): InspectionEstimateWorkflow {
    return InspectionEstimateWorkflow(
        store = store,
        validator = defaultInspectionEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
