package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.safety.clock.Clock

/** Moves default scorecard assignments through their life cycle and keeps a journal of what it did. */
data class DefaultScorecardAssignmentCoordinator(
    var store: DefaultScorecardAssignmentStore,
    var validator: DefaultScorecardAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default scorecard assignment $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultScorecardAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default scorecard assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultScorecardAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDefaultScorecardAssignmentCoordinator(store: DefaultScorecardAssignmentStore, clock: Clock): DefaultScorecardAssignmentCoordinator {
    return DefaultScorecardAssignmentCoordinator(
        store = store,
        validator = defaultDefaultScorecardAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
