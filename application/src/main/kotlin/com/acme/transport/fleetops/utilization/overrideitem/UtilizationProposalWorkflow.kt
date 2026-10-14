package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization proposals through their life cycle and keeps a journal of what it did. */
data class UtilizationProposalService(
    var store: UtilizationProposalStore,
    var validator: UtilizationProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("utilization proposal $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationProposalStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationProposalStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newUtilizationProposalService(store: UtilizationProposalStore, clock: Clock): UtilizationProposalService {
    return UtilizationProposalService(
        store = store,
        validator = defaultUtilizationProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
