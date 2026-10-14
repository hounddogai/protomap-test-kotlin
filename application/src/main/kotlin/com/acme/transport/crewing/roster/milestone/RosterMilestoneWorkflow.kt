package com.acme.transport.crewing.roster.milestone

import com.acme.transport.crewing.clock.Clock

/** Moves roster milestones through their life cycle and keeps a journal of what it did. */
data class RosterMilestoneManager(
    var store: RosterMilestoneStore,
    var validator: RosterMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("roster milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RosterMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("roster milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRosterMilestoneManager(store: RosterMilestoneStore, clock: Clock): RosterMilestoneManager {
    return RosterMilestoneManager(
        store = store,
        validator = defaultRosterMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
