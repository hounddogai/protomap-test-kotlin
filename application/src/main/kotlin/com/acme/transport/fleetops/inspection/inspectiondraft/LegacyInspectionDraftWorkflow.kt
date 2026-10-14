package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy inspection drafts through their life cycle and keeps a journal of what it did. */
data class LegacyInspectionDraftManager(
    var store: LegacyInspectionDraftStore,
    var validator: LegacyInspectionDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy inspection draft $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyInspectionDraftStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy inspection draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyInspectionDraftStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLegacyInspectionDraftManager(store: LegacyInspectionDraftStore, clock: Clock): LegacyInspectionDraftManager {
    return LegacyInspectionDraftManager(
        store = store,
        validator = defaultLegacyInspectionDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
