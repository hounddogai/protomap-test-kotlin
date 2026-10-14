package com.acme.transport.crewing.roster.review

import com.acme.transport.crewing.clock.Clock

/** Moves weighted roster drafts through their life cycle and keeps a journal of what it did. */
data class WeightedRosterDraftManager(
    var store: WeightedRosterDraftStore,
    var validator: WeightedRosterDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted roster draft $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRosterDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted roster draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRosterDraftStatus.CLOSED)
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

fun newWeightedRosterDraftManager(store: WeightedRosterDraftStore, clock: Clock): WeightedRosterDraftManager {
    return WeightedRosterDraftManager(
        store = store,
        validator = defaultWeightedRosterDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
