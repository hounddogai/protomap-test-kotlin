package com.acme.transport.safety.coaching.digest

import com.acme.transport.safety.clock.Clock

/** Moves coaching agreements through their life cycle and keeps a journal of what it did. */
data class CoachingAgreementManager(
    var store: CoachingAgreementStore,
    var validator: CoachingAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("coaching agreement $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingAgreementStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingAgreementStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCoachingAgreementManager(store: CoachingAgreementStore, clock: Clock): CoachingAgreementManager {
    return CoachingAgreementManager(
        store = store,
        validator = defaultCoachingAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
