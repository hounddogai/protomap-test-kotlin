package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.safety.clock.Clock

/** Moves scorecard reviews through their life cycle and keeps a journal of what it did. */
data class ScorecardReviewManager(
    var store: ScorecardReviewStore,
    var validator: ScorecardReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scorecard review $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardReviewStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardReviewStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newScorecardReviewManager(store: ScorecardReviewStore, clock: Clock): ScorecardReviewManager {
    return ScorecardReviewManager(
        store = store,
        validator = defaultScorecardReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
