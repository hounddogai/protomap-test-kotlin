package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.customs.clock.Clock

/** Moves clearance proposals through their life cycle and keeps a journal of what it did. */
data class ClearanceProposalService(
    var store: ClearanceProposalStore,
    var validator: ClearanceProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("clearance proposal $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceProposalStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceProposalStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newClearanceProposalService(store: ClearanceProposalStore, clock: Clock): ClearanceProposalService {
    return ClearanceProposalService(
        store = store,
        validator = defaultClearanceProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
