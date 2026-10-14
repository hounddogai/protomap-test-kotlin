package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.safety.clock.Clock

/** Moves fallback scorecard targets through their life cycle and keeps a journal of what it did. */
data class FallbackScorecardTargetCoordinator(
    var store: FallbackScorecardTargetStore,
    var validator: FallbackScorecardTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback scorecard target $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackScorecardTargetStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback scorecard target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackScorecardTargetStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newFallbackScorecardTargetCoordinator(store: FallbackScorecardTargetStore, clock: Clock): FallbackScorecardTargetCoordinator {
    return FallbackScorecardTargetCoordinator(
        store = store,
        validator = defaultFallbackScorecardTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
