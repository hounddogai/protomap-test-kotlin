package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer proposals through their life cycle and keeps a journal of what it did. */
data class TrailerProposalManager(
    var store: TrailerProposalStore,
    var validator: TrailerProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("trailer proposal $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerProposalStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerProposalStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newTrailerProposalManager(store: TrailerProposalStore, clock: Clock): TrailerProposalManager {
    return TrailerProposalManager(
        store = store,
        validator = defaultTrailerProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
