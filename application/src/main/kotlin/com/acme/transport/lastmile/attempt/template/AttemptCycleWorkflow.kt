package com.acme.transport.lastmile.attempt.template

import com.acme.transport.lastmile.clock.Clock

/** Moves attempt cycles through their life cycle and keeps a journal of what it did. */
data class AttemptCycleWorkflow(
    var store: AttemptCycleStore,
    var validator: AttemptCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("attempt cycle $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptCycleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptCycleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newAttemptCycleWorkflow(store: AttemptCycleStore, clock: Clock): AttemptCycleWorkflow {
    return AttemptCycleWorkflow(
        store = store,
        validator = defaultAttemptCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
