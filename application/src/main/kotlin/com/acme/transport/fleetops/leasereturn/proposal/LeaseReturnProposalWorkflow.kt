package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return proposals through their life cycle and keeps a journal of what it did. */
data class LeaseReturnProposalService(
    var store: LeaseReturnProposalStore,
    var validator: LeaseReturnProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lease return proposal $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnProposalStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnProposalStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLeaseReturnProposalService(store: LeaseReturnProposalStore, clock: Clock): LeaseReturnProposalService {
    return LeaseReturnProposalService(
        store = store,
        validator = defaultLeaseReturnProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
