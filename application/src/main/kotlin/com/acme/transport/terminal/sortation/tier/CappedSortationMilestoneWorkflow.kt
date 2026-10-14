package com.acme.transport.terminal.sortation.tier

import com.acme.transport.terminal.clock.Clock

/** Moves capped sortation milestones through their life cycle and keeps a journal of what it did. */
data class CappedSortationMilestoneDesk(
    var store: CappedSortationMilestoneStore,
    var validator: CappedSortationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped sortation milestone $code not found")
        validator.requireValid(item)
        item.moveTo(CappedSortationMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped sortation milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedSortationMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newCappedSortationMilestoneDesk(store: CappedSortationMilestoneStore, clock: Clock): CappedSortationMilestoneDesk {
    return CappedSortationMilestoneDesk(
        store = store,
        validator = defaultCappedSortationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
