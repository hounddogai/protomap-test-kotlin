package com.acme.transport.crewing.roster.agreement

import com.acme.transport.crewing.clock.Clock

/** Moves default roster reviews through their life cycle and keeps a journal of what it did. */
data class DefaultRosterReviewWorkflow(
    var store: DefaultRosterReviewStore,
    var validator: DefaultRosterReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default roster review $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRosterReviewStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default roster review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRosterReviewStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newDefaultRosterReviewWorkflow(store: DefaultRosterReviewStore, clock: Clock): DefaultRosterReviewWorkflow {
    return DefaultRosterReviewWorkflow(
        store = store,
        validator = defaultDefaultRosterReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
