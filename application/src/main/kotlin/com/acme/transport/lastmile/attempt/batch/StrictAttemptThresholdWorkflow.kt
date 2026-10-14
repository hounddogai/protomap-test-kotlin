package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.lastmile.clock.Clock

/** Moves strict attempt thresholds through their life cycle and keeps a journal of what it did. */
data class StrictAttemptThresholdCoordinator(
    var store: StrictAttemptThresholdStore,
    var validator: StrictAttemptThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict attempt threshold $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAttemptThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict attempt threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAttemptThresholdStatus.ARCHIVED)
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

fun newStrictAttemptThresholdCoordinator(store: StrictAttemptThresholdStore, clock: Clock): StrictAttemptThresholdCoordinator {
    return StrictAttemptThresholdCoordinator(
        store = store,
        validator = defaultStrictAttemptThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
