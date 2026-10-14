package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.clock.Clock

/** Moves strict roster segments through their life cycle and keeps a journal of what it did. */
data class StrictRosterSegmentDesk(
    var store: StrictRosterSegmentStore,
    var validator: StrictRosterSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict roster segment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRosterSegmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict roster segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRosterSegmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newStrictRosterSegmentDesk(store: StrictRosterSegmentStore, clock: Clock): StrictRosterSegmentDesk {
    return StrictRosterSegmentDesk(
        store = store,
        validator = defaultStrictRosterSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
