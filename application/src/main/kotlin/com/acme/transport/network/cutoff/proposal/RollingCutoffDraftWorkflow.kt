package com.acme.transport.network.cutoff.proposal

import com.acme.transport.network.clock.Clock

/** Moves rolling cutoff drafts through their life cycle and keeps a journal of what it did. */
data class RollingCutoffDraftCoordinator(
    var store: RollingCutoffDraftStore,
    var validator: RollingCutoffDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling cutoff draft $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCutoffDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling cutoff draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCutoffDraftStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newRollingCutoffDraftCoordinator(store: RollingCutoffDraftStore, clock: Clock): RollingCutoffDraftCoordinator {
    return RollingCutoffDraftCoordinator(
        store = store,
        validator = defaultRollingCutoffDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
