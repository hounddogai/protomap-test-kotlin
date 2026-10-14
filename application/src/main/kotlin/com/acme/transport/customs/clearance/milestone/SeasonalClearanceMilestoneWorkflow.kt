package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.clock.Clock

/** Moves seasonal clearance milestones through their life cycle and keeps a journal of what it did. */
data class SeasonalClearanceMilestoneDesk(
    var store: SeasonalClearanceMilestoneStore,
    var validator: SeasonalClearanceMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal clearance milestone $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalClearanceMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal clearance milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalClearanceMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newSeasonalClearanceMilestoneDesk(store: SeasonalClearanceMilestoneStore, clock: Clock): SeasonalClearanceMilestoneDesk {
    return SeasonalClearanceMilestoneDesk(
        store = store,
        validator = defaultSeasonalClearanceMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
