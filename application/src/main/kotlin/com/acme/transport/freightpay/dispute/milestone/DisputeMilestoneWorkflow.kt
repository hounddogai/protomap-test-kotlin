package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute milestones through their life cycle and keeps a journal of what it did. */
data class DisputeMilestoneCoordinator(
    var store: DisputeMilestoneStore,
    var validator: DisputeMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dispute milestone $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newDisputeMilestoneCoordinator(store: DisputeMilestoneStore, clock: Clock): DisputeMilestoneCoordinator {
    return DisputeMilestoneCoordinator(
        store = store,
        validator = defaultDisputeMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
