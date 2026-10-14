package com.acme.transport.network.transittime.batch

import com.acme.transport.network.clock.Clock

/** Moves transit time milestones through their life cycle and keeps a journal of what it did. */
data class TransitTimeMilestoneManager(
    var store: TransitTimeMilestoneStore,
    var validator: TransitTimeMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("transit time milestone $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTransitTimeMilestoneManager(store: TransitTimeMilestoneStore, clock: Clock): TransitTimeMilestoneManager {
    return TransitTimeMilestoneManager(
        store = store,
        validator = defaultTransitTimeMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
