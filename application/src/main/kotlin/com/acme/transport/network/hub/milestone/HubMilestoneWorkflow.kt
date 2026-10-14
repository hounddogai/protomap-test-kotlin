package com.acme.transport.network.hub.milestone

import com.acme.transport.network.clock.Clock

/** Moves hub milestones through their life cycle and keeps a journal of what it did. */
data class HubMilestoneService(
    var store: HubMilestoneStore,
    var validator: HubMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("hub milestone $code not found")
        validator.requireValid(item)
        item.moveTo(HubMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newHubMilestoneService(store: HubMilestoneStore, clock: Clock): HubMilestoneService {
    return HubMilestoneService(
        store = store,
        validator = defaultHubMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
