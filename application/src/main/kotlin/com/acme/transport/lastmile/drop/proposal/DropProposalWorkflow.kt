package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.lastmile.clock.Clock

/** Moves drop proposals through their life cycle and keeps a journal of what it did. */
data class DropProposalCoordinator(
    var store: DropProposalStore,
    var validator: DropProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("drop proposal $code not found")
        validator.requireValid(item)
        item.moveTo(DropProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("drop proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newDropProposalCoordinator(store: DropProposalStore, clock: Clock): DropProposalCoordinator {
    return DropProposalCoordinator(
        store = store,
        validator = defaultDropProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
