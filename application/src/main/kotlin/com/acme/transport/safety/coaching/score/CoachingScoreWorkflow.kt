package com.acme.transport.safety.coaching.score

import com.acme.transport.safety.clock.Clock

/** Moves coaching scores through their life cycle and keeps a journal of what it did. */
data class CoachingScoreCoordinator(
    var store: CoachingScoreStore,
    var validator: CoachingScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("coaching score $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newCoachingScoreCoordinator(store: CoachingScoreStore, clock: Clock): CoachingScoreCoordinator {
    return CoachingScoreCoordinator(
        store = store,
        validator = defaultCoachingScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
