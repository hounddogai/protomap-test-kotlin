package com.acme.transport.safety.violation.entry

import com.acme.transport.safety.clock.Clock

/** Moves strict violation proposals through their life cycle and keeps a journal of what it did. */
data class StrictViolationProposalService(
    var store: StrictViolationProposalStore,
    var validator: StrictViolationProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict violation proposal $code not found")
        validator.requireValid(item)
        item.moveTo(StrictViolationProposalStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict violation proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictViolationProposalStatus.WITHDRAWN)
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

fun newStrictViolationProposalService(store: StrictViolationProposalStore, clock: Clock): StrictViolationProposalService {
    return StrictViolationProposalService(
        store = store,
        validator = defaultStrictViolationProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
