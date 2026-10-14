package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock drafts through their life cycle and keeps a journal of what it did. */
data class CrossDockDraftCoordinator(
    var store: CrossDockDraftStore,
    var validator: CrossDockDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cross dock draft $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockDraftStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockDraftStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newCrossDockDraftCoordinator(store: CrossDockDraftStore, clock: Clock): CrossDockDraftCoordinator {
    return CrossDockDraftCoordinator(
        store = store,
        validator = defaultCrossDockDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
