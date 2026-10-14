package com.acme.transport.network.hub.hubplan

import com.acme.transport.network.clock.Clock

/** Moves legacy hub proposals through their life cycle and keeps a journal of what it did. */
data class LegacyHubProposalCoordinator(
    var store: LegacyHubProposalStore,
    var validator: LegacyHubProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy hub proposal $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyHubProposalStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy hub proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyHubProposalStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newLegacyHubProposalCoordinator(store: LegacyHubProposalStore, clock: Clock): LegacyHubProposalCoordinator {
    return LegacyHubProposalCoordinator(
        store = store,
        validator = defaultLegacyHubProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
