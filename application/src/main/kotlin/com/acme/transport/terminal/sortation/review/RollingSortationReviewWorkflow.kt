package com.acme.transport.terminal.sortation.review

import com.acme.transport.terminal.clock.Clock

/** Moves rolling sortation reviews through their life cycle and keeps a journal of what it did. */
data class RollingSortationReviewDesk(
    var store: RollingSortationReviewStore,
    var validator: RollingSortationReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling sortation review $code not found")
        validator.requireValid(item)
        item.moveTo(RollingSortationReviewStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling sortation review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingSortationReviewStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newRollingSortationReviewDesk(store: RollingSortationReviewStore, clock: Clock): RollingSortationReviewDesk {
    return RollingSortationReviewDesk(
        store = store,
        validator = defaultRollingSortationReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
