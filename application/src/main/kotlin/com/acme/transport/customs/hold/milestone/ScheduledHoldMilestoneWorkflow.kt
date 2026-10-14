package com.acme.transport.customs.hold.milestone

import com.acme.transport.customs.clock.Clock

/** Moves scheduled hold milestones through their life cycle and keeps a journal of what it did. */
data class ScheduledHoldMilestoneManager(
    var store: ScheduledHoldMilestoneStore,
    var validator: ScheduledHoldMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled hold milestone $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledHoldMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled hold milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledHoldMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newScheduledHoldMilestoneManager(store: ScheduledHoldMilestoneStore, clock: Clock): ScheduledHoldMilestoneManager {
    return ScheduledHoldMilestoneManager(
        store = store,
        validator = defaultScheduledHoldMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
