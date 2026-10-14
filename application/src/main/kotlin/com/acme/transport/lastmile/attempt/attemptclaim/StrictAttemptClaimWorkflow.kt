package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.lastmile.clock.Clock

/** Moves strict attempt claims through their life cycle and keeps a journal of what it did. */
data class StrictAttemptClaimCoordinator(
    var store: StrictAttemptClaimStore,
    var validator: StrictAttemptClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictAttemptClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict attempt claim $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAttemptClaimStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict attempt claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAttemptClaimStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict attempt claim $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newStrictAttemptClaimCoordinator(store: StrictAttemptClaimStore, clock: Clock): StrictAttemptClaimCoordinator {
    return StrictAttemptClaimCoordinator(
        store = store,
        validator = defaultStrictAttemptClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictAttemptClaimEngine(),
    )
}
