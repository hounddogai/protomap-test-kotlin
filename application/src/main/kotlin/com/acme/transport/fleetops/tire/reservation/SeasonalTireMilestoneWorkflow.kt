package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.clock.Clock

/** Moves seasonal tire milestones through their life cycle and keeps a journal of what it did. */
data class SeasonalTireMilestoneCoordinator(
    var store: SeasonalTireMilestoneStore,
    var validator: SeasonalTireMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal tire milestone $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTireMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal tire milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTireMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newSeasonalTireMilestoneCoordinator(store: SeasonalTireMilestoneStore, clock: Clock): SeasonalTireMilestoneCoordinator {
    return SeasonalTireMilestoneCoordinator(
        store = store,
        validator = defaultSeasonalTireMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
