package com.acme.transport.crewing.roster.quota

import com.acme.transport.crewing.clock.Clock

/** Moves scheduled roster profiles through their life cycle and keeps a journal of what it did. */
data class ScheduledRosterProfileWorkflow(
    var store: ScheduledRosterProfileStore,
    var validator: ScheduledRosterProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled roster profile $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRosterProfileStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled roster profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRosterProfileStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newScheduledRosterProfileWorkflow(store: ScheduledRosterProfileStore, clock: Clock): ScheduledRosterProfileWorkflow {
    return ScheduledRosterProfileWorkflow(
        store = store,
        validator = defaultScheduledRosterProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
