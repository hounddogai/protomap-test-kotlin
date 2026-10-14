package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.fleetops.clock.Clock

/** Moves pending defect reviews through their life cycle and keeps a journal of what it did. */
data class PendingDefectReviewWorkflow(
    var store: PendingDefectReviewStore,
    var validator: PendingDefectReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending defect review $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDefectReviewStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending defect review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDefectReviewStatus.EXPIRED)
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

fun newPendingDefectReviewWorkflow(store: PendingDefectReviewStore, clock: Clock): PendingDefectReviewWorkflow {
    return PendingDefectReviewWorkflow(
        store = store,
        validator = defaultPendingDefectReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
