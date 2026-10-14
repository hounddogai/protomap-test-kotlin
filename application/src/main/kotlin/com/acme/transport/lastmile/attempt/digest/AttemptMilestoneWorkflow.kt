package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.lastmile.clock.Clock

/** Moves attempt milestones through their life cycle and keeps a journal of what it did. */
data class AttemptMilestoneWorkflow(
    var store: AttemptMilestoneStore,
    var validator: AttemptMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("attempt milestone $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAttemptMilestoneWorkflow(store: AttemptMilestoneStore, clock: Clock): AttemptMilestoneWorkflow {
    return AttemptMilestoneWorkflow(
        store = store,
        validator = defaultAttemptMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
