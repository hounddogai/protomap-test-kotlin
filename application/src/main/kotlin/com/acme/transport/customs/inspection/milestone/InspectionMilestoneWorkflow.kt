package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.clock.Clock

/** Moves inspection milestones through their life cycle and keeps a journal of what it did. */
data class InspectionMilestoneDesk(
    var store: InspectionMilestoneStore,
    var validator: InspectionMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection milestone $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("inspection milestone $code not found")
        val result = engine.run(item)
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

fun newInspectionMilestoneDesk(store: InspectionMilestoneStore, clock: Clock): InspectionMilestoneDesk {
    return InspectionMilestoneDesk(
        store = store,
        validator = defaultInspectionMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionMilestoneEngine(),
    )
}
