package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.lastmile.clock.Clock

/** Moves drop milestones through their life cycle and keeps a journal of what it did. */
data class DropMilestoneCoordinator(
    var store: DropMilestoneStore,
    var validator: DropMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("drop milestone $code not found")
        validator.requireValid(item)
        item.moveTo(DropMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("drop milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropMilestoneStatus.STOPPED)
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

fun newDropMilestoneCoordinator(store: DropMilestoneStore, clock: Clock): DropMilestoneCoordinator {
    return DropMilestoneCoordinator(
        store = store,
        validator = defaultDropMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
