package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.clock.Clock

/** Moves tiered roster milestones through their life cycle and keeps a journal of what it did. */
data class TieredRosterMilestoneCoordinator(
    var store: TieredRosterMilestoneStore,
    var validator: TieredRosterMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered roster milestone $code not found")
        validator.requireValid(item)
        item.moveTo(TieredRosterMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered roster milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredRosterMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newTieredRosterMilestoneCoordinator(store: TieredRosterMilestoneStore, clock: Clock): TieredRosterMilestoneCoordinator {
    return TieredRosterMilestoneCoordinator(
        store = store,
        validator = defaultTieredRosterMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
