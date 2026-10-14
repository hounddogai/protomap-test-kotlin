package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.clock.Clock

/** Moves rolling rest break milestones through their life cycle and keeps a journal of what it did. */
data class RollingRestBreakMilestoneWorkflow(
    var store: RollingRestBreakMilestoneStore,
    var validator: RollingRestBreakMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling rest break milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRestBreakMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling rest break milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRestBreakMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newRollingRestBreakMilestoneWorkflow(store: RollingRestBreakMilestoneStore, clock: Clock): RollingRestBreakMilestoneWorkflow {
    return RollingRestBreakMilestoneWorkflow(
        store = store,
        validator = defaultRollingRestBreakMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
