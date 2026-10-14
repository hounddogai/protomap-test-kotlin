package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.terminal.clock.Clock

/** Moves legacy sortation reviews through their life cycle and keeps a journal of what it did. */
data class LegacySortationReviewWorkflow(
    var store: LegacySortationReviewStore,
    var validator: LegacySortationReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy sortation review $code not found")
        validator.requireValid(item)
        item.moveTo(LegacySortationReviewStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy sortation review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacySortationReviewStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLegacySortationReviewWorkflow(store: LegacySortationReviewStore, clock: Clock): LegacySortationReviewWorkflow {
    return LegacySortationReviewWorkflow(
        store = store,
        validator = defaultLegacySortationReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
