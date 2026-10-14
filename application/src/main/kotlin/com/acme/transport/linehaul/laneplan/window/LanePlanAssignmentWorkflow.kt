package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan assignments through their life cycle and keeps a journal of what it did. */
data class LanePlanAssignmentWorkflow(
    var store: LanePlanAssignmentStore,
    var validator: LanePlanAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanAssignmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanAssignmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newLanePlanAssignmentWorkflow(store: LanePlanAssignmentStore, clock: Clock): LanePlanAssignmentWorkflow {
    return LanePlanAssignmentWorkflow(
        store = store,
        validator = defaultLanePlanAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
