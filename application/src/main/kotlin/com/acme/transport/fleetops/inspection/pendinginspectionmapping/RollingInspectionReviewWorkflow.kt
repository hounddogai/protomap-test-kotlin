package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.fleetops.clock.Clock

/** Moves rolling inspection reviews through their life cycle and keeps a journal of what it did. */
data class RollingInspectionReviewCoordinator(
    var store: RollingInspectionReviewStore,
    var validator: RollingInspectionReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling inspection review $code not found")
        validator.requireValid(item)
        item.moveTo(RollingInspectionReviewStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling inspection review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingInspectionReviewStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRollingInspectionReviewCoordinator(store: RollingInspectionReviewStore, clock: Clock): RollingInspectionReviewCoordinator {
    return RollingInspectionReviewCoordinator(
        store = store,
        validator = defaultRollingInspectionReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
