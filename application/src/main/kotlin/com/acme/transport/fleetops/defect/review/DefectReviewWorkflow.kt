package com.acme.transport.fleetops.defect.review

import com.acme.transport.fleetops.clock.Clock

/** Moves defect reviews through their life cycle and keeps a journal of what it did. */
data class DefectReviewService(
    var store: DefectReviewStore,
    var validator: DefectReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("defect review $code not found")
        validator.requireValid(item)
        item.moveTo(DefectReviewStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("defect review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectReviewStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefectReviewService(store: DefectReviewStore, clock: Clock): DefectReviewService {
    return DefectReviewService(
        store = store,
        validator = defaultDefectReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
