package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.lastmile.clock.Clock

/** Moves tiered attempt baselines through their life cycle and keeps a journal of what it did. */
data class TieredAttemptBaselineCoordinator(
    var store: TieredAttemptBaselineStore,
    var validator: TieredAttemptBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered attempt baseline $code not found")
        validator.requireValid(item)
        item.moveTo(TieredAttemptBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered attempt baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredAttemptBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newTieredAttemptBaselineCoordinator(store: TieredAttemptBaselineStore, clock: Clock): TieredAttemptBaselineCoordinator {
    return TieredAttemptBaselineCoordinator(
        store = store,
        validator = defaultTieredAttemptBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
