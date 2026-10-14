package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.clock.Clock

/** Moves strict utilization reviews through their life cycle and keeps a journal of what it did. */
data class StrictUtilizationReviewService(
    var store: StrictUtilizationReviewStore,
    var validator: StrictUtilizationReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict utilization review $code not found")
        validator.requireValid(item)
        item.moveTo(StrictUtilizationReviewStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict utilization review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictUtilizationReviewStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newStrictUtilizationReviewService(store: StrictUtilizationReviewStore, clock: Clock): StrictUtilizationReviewService {
    return StrictUtilizationReviewService(
        store = store,
        validator = defaultStrictUtilizationReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
