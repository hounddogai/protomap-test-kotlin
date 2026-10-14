package com.acme.transport.safety.scorecard.segment

import com.acme.transport.safety.clock.Clock

/** Moves capped scorecard segments through their life cycle and keeps a journal of what it did. */
data class CappedScorecardSegmentCoordinator(
    var store: CappedScorecardSegmentStore,
    var validator: CappedScorecardSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped scorecard segment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedScorecardSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped scorecard segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedScorecardSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newCappedScorecardSegmentCoordinator(store: CappedScorecardSegmentStore, clock: Clock): CappedScorecardSegmentCoordinator {
    return CappedScorecardSegmentCoordinator(
        store = store,
        validator = defaultCappedScorecardSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
