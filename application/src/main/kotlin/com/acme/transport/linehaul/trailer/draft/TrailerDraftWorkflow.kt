package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer drafts through their life cycle and keeps a journal of what it did. */
data class TrailerDraftDesk(
    var store: TrailerDraftStore,
    var validator: TrailerDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer draft $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerDraftStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTrailerDraftDesk(store: TrailerDraftStore, clock: Clock): TrailerDraftDesk {
    return TrailerDraftDesk(
        store = store,
        validator = defaultTrailerDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
