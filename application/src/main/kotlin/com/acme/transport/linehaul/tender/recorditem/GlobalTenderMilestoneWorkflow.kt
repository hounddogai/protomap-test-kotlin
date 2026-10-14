package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.linehaul.clock.Clock

/** Moves global tender milestones through their life cycle and keeps a journal of what it did. */
data class GlobalTenderMilestoneWorkflow(
    var store: GlobalTenderMilestoneStore,
    var validator: GlobalTenderMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global tender milestone $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTenderMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global tender milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTenderMilestoneStatus.STOPPED)
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

fun newGlobalTenderMilestoneWorkflow(store: GlobalTenderMilestoneStore, clock: Clock): GlobalTenderMilestoneWorkflow {
    return GlobalTenderMilestoneWorkflow(
        store = store,
        validator = defaultGlobalTenderMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
