package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection drafts through their life cycle and keeps a journal of what it did. */
data class InspectionDraftService(
    var store: InspectionDraftStore,
    var validator: InspectionDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("inspection draft $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionDraftStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newInspectionDraftService(store: InspectionDraftStore, clock: Clock): InspectionDraftService {
    return InspectionDraftService(
        store = store,
        validator = defaultInspectionDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
