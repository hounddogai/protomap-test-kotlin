package com.acme.transport.fleetops.defect.review

import com.acme.transport.fleetops.clock.Clock

/** Moves defect drafts through their life cycle and keeps a journal of what it did. */
data class DefectDraftWorkflow(
    var store: DefectDraftStore,
    var validator: DefectDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("defect draft $code not found")
        validator.requireValid(item)
        item.moveTo(DefectDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("defect draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectDraftStatus.CLOSED)
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

fun newDefectDraftWorkflow(store: DefectDraftStore, clock: Clock): DefectDraftWorkflow {
    return DefectDraftWorkflow(
        store = store,
        validator = defaultDefectDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
