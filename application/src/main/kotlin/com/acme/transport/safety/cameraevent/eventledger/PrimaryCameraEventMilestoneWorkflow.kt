package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.safety.clock.Clock

/**
 * Moves primary camera event milestones through their life cycle and keeps a journal of what it did.
 */
data class PrimaryCameraEventMilestoneCoordinator(
    var store: PrimaryCameraEventMilestoneStore,
    var validator: PrimaryCameraEventMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary camera event milestone $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCameraEventMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary camera event milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCameraEventMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newPrimaryCameraEventMilestoneCoordinator(store: PrimaryCameraEventMilestoneStore, clock: Clock): PrimaryCameraEventMilestoneCoordinator {
    return PrimaryCameraEventMilestoneCoordinator(
        store = store,
        validator = defaultPrimaryCameraEventMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
