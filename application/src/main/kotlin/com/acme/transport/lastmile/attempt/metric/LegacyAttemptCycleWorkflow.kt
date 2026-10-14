package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.lastmile.clock.Clock

/** Moves legacy attempt cycles through their life cycle and keeps a journal of what it did. */
data class LegacyAttemptCycleService(
    var store: LegacyAttemptCycleStore,
    var validator: LegacyAttemptCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy attempt cycle $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAttemptCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy attempt cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAttemptCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLegacyAttemptCycleService(store: LegacyAttemptCycleStore, clock: Clock): LegacyAttemptCycleService {
    return LegacyAttemptCycleService(
        store = store,
        validator = defaultLegacyAttemptCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
