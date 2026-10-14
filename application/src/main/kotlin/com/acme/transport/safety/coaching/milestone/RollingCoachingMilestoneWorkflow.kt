package com.acme.transport.safety.coaching.milestone

import com.acme.transport.safety.clock.Clock

/** Moves rolling coaching milestones through their life cycle and keeps a journal of what it did. */
data class RollingCoachingMilestoneDesk(
    var store: RollingCoachingMilestoneStore,
    var validator: RollingCoachingMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling coaching milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCoachingMilestoneStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling coaching milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCoachingMilestoneStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRollingCoachingMilestoneDesk(store: RollingCoachingMilestoneStore, clock: Clock): RollingCoachingMilestoneDesk {
    return RollingCoachingMilestoneDesk(
        store = store,
        validator = defaultRollingCoachingMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
