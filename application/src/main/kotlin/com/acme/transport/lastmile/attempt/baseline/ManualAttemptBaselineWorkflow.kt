package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.lastmile.clock.Clock

/** Moves manual attempt baselines through their life cycle and keeps a journal of what it did. */
data class ManualAttemptBaselineCoordinator(
    var store: ManualAttemptBaselineStore,
    var validator: ManualAttemptBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual attempt baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAttemptBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual attempt baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAttemptBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newManualAttemptBaselineCoordinator(store: ManualAttemptBaselineStore, clock: Clock): ManualAttemptBaselineCoordinator {
    return ManualAttemptBaselineCoordinator(
        store = store,
        validator = defaultManualAttemptBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
