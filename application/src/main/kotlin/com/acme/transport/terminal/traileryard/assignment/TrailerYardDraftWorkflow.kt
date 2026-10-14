package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard drafts through their life cycle and keeps a journal of what it did. */
data class TrailerYardDraftManager(
    var store: TrailerYardDraftStore,
    var validator: TrailerYardDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("trailer yard draft $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardDraftStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardDraftStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newTrailerYardDraftManager(store: TrailerYardDraftStore, clock: Clock): TrailerYardDraftManager {
    return TrailerYardDraftManager(
        store = store,
        validator = defaultTrailerYardDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
