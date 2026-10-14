package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.safety.clock.Clock

/** Moves near miss milestones through their life cycle and keeps a journal of what it did. */
data class NearMissMilestoneCoordinator(
    var store: NearMissMilestoneStore,
    var validator: NearMissMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("near miss milestone $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newNearMissMilestoneCoordinator(store: NearMissMilestoneStore, clock: Clock): NearMissMilestoneCoordinator {
    return NearMissMilestoneCoordinator(
        store = store,
        validator = defaultNearMissMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
