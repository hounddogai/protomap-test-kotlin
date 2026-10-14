package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.terminal.clock.Clock

/** Moves rolling dock door proposals through their life cycle and keeps a journal of what it did. */
data class RollingDockDoorProposalCoordinator(
    var store: RollingDockDoorProposalStore,
    var validator: RollingDockDoorProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling dock door proposal $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDockDoorProposalStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dock door proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDockDoorProposalStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newRollingDockDoorProposalCoordinator(store: RollingDockDoorProposalStore, clock: Clock): RollingDockDoorProposalCoordinator {
    return RollingDockDoorProposalCoordinator(
        store = store,
        validator = defaultRollingDockDoorProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
