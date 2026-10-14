package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.clock.Clock

/** Moves pending availability reviews through their life cycle and keeps a journal of what it did. */
data class PendingAvailabilityReviewWorkflow(
    var store: PendingAvailabilityReviewStore,
    var validator: PendingAvailabilityReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingAvailabilityReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending availability review $code not found")
        validator.requireValid(item)
        item.moveTo(PendingAvailabilityReviewStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending availability review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingAvailabilityReviewStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending availability review $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingAvailabilityReviewWorkflow(store: PendingAvailabilityReviewStore, clock: Clock): PendingAvailabilityReviewWorkflow {
    return PendingAvailabilityReviewWorkflow(
        store = store,
        validator = defaultPendingAvailabilityReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingAvailabilityReviewEngine(),
    )
}
