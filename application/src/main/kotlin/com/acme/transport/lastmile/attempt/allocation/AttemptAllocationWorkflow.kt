package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.lastmile.clock.Clock

/** Moves attempt allocations through their life cycle and keeps a journal of what it did. */
data class AttemptAllocationWorkflow(
    var store: AttemptAllocationStore,
    var validator: AttemptAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("attempt allocation $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptAllocationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptAllocationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newAttemptAllocationWorkflow(store: AttemptAllocationStore, clock: Clock): AttemptAllocationWorkflow {
    return AttemptAllocationWorkflow(
        store = store,
        validator = defaultAttemptAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
