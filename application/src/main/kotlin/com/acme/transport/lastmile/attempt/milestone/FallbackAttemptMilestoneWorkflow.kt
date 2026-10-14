package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves fallback attempt milestones through their life cycle and keeps a journal of what it did. */
data class FallbackAttemptMilestoneCoordinator(
    var store: FallbackAttemptMilestoneStore,
    var validator: FallbackAttemptMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackAttemptMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback attempt milestone $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackAttemptMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback attempt milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackAttemptMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback attempt milestone $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newFallbackAttemptMilestoneCoordinator(store: FallbackAttemptMilestoneStore, clock: Clock, flags: Registry): FallbackAttemptMilestoneCoordinator {
    return FallbackAttemptMilestoneCoordinator(
        store = store,
        validator = defaultFallbackAttemptMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackAttemptMilestoneEngine(flags),
    )
}
