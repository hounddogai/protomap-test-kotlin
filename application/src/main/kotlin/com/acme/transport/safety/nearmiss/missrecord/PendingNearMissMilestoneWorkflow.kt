package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.safety.clock.Clock

/** Moves pending near miss milestones through their life cycle and keeps a journal of what it did. */
data class PendingNearMissMilestoneWorkflow(
    var store: PendingNearMissMilestoneStore,
    var validator: PendingNearMissMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending near miss milestone $code not found")
        validator.requireValid(item)
        item.moveTo(PendingNearMissMilestoneStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending near miss milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingNearMissMilestoneStatus.WITHDRAWN)
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

fun newPendingNearMissMilestoneWorkflow(store: PendingNearMissMilestoneStore, clock: Clock): PendingNearMissMilestoneWorkflow {
    return PendingNearMissMilestoneWorkflow(
        store = store,
        validator = defaultPendingNearMissMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
