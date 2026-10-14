package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.lastmile.clock.Clock

/** Moves global attempt drafts through their life cycle and keeps a journal of what it did. */
data class GlobalAttemptDraftManager(
    var store: GlobalAttemptDraftStore,
    var validator: GlobalAttemptDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global attempt draft $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalAttemptDraftStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global attempt draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalAttemptDraftStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalAttemptDraftManager(store: GlobalAttemptDraftStore, clock: Clock): GlobalAttemptDraftManager {
    return GlobalAttemptDraftManager(
        store = store,
        validator = defaultGlobalAttemptDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
