package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.safety.clock.Clock

/** Moves scorecard allocations through their life cycle and keeps a journal of what it did. */
data class ScorecardAllocationDesk(
    var store: ScorecardAllocationStore,
    var validator: ScorecardAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scorecard allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardAllocationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardAllocationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newScorecardAllocationDesk(store: ScorecardAllocationStore, clock: Clock): ScorecardAllocationDesk {
    return ScorecardAllocationDesk(
        store = store,
        validator = defaultScorecardAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
