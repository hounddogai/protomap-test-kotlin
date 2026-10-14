package com.acme.transport.network.node.nodedraft

import com.acme.transport.network.clock.Clock

/** Moves primary node milestones through their life cycle and keeps a journal of what it did. */
data class PrimaryNodeMilestoneDesk(
    var store: PrimaryNodeMilestoneStore,
    var validator: PrimaryNodeMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary node milestone $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryNodeMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary node milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryNodeMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newPrimaryNodeMilestoneDesk(store: PrimaryNodeMilestoneStore, clock: Clock): PrimaryNodeMilestoneDesk {
    return PrimaryNodeMilestoneDesk(
        store = store,
        validator = defaultPrimaryNodeMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
