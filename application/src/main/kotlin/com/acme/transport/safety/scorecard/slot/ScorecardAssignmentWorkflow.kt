package com.acme.transport.safety.scorecard.slot

import com.acme.transport.safety.clock.Clock

/** Moves scorecard assignments through their life cycle and keeps a journal of what it did. */
data class ScorecardAssignmentWorkflow(
    var store: ScorecardAssignmentStore,
    var validator: ScorecardAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scorecard assignment $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newScorecardAssignmentWorkflow(store: ScorecardAssignmentStore, clock: Clock): ScorecardAssignmentWorkflow {
    return ScorecardAssignmentWorkflow(
        store = store,
        validator = defaultScorecardAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
