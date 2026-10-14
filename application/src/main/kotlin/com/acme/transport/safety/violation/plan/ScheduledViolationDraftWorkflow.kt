package com.acme.transport.safety.violation.plan

import com.acme.transport.safety.clock.Clock

/** Moves scheduled violation drafts through their life cycle and keeps a journal of what it did. */
data class ScheduledViolationDraftManager(
    var store: ScheduledViolationDraftStore,
    var validator: ScheduledViolationDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled violation draft $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledViolationDraftStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled violation draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledViolationDraftStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newScheduledViolationDraftManager(store: ScheduledViolationDraftStore, clock: Clock): ScheduledViolationDraftManager {
    return ScheduledViolationDraftManager(
        store = store,
        validator = defaultScheduledViolationDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
