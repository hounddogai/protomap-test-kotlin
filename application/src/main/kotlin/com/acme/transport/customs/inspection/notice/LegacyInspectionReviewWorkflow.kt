package com.acme.transport.customs.inspection.notice

import com.acme.transport.customs.clock.Clock

/** Moves legacy inspection reviews through their life cycle and keeps a journal of what it did. */
data class LegacyInspectionReviewCoordinator(
    var store: LegacyInspectionReviewStore,
    var validator: LegacyInspectionReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy inspection review $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyInspectionReviewStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy inspection review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyInspectionReviewStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLegacyInspectionReviewCoordinator(store: LegacyInspectionReviewStore, clock: Clock): LegacyInspectionReviewCoordinator {
    return LegacyInspectionReviewCoordinator(
        store = store,
        validator = defaultLegacyInspectionReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
