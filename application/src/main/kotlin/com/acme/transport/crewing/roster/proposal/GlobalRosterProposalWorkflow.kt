package com.acme.transport.crewing.roster.proposal

import com.acme.transport.crewing.clock.Clock

/** Moves global roster proposals through their life cycle and keeps a journal of what it did. */
data class GlobalRosterProposalCoordinator(
    var store: GlobalRosterProposalStore,
    var validator: GlobalRosterProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global roster proposal $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRosterProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global roster proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRosterProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newGlobalRosterProposalCoordinator(store: GlobalRosterProposalStore, clock: Clock): GlobalRosterProposalCoordinator {
    return GlobalRosterProposalCoordinator(
        store = store,
        validator = defaultGlobalRosterProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
