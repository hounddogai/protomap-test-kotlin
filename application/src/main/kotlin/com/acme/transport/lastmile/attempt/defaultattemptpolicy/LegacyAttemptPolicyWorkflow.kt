package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.lastmile.clock.Clock

/** Moves legacy attempt policies through their life cycle and keeps a journal of what it did. */
data class LegacyAttemptPolicyManager(
    var store: LegacyAttemptPolicyStore,
    var validator: LegacyAttemptPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy attempt policy $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAttemptPolicyStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy attempt policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAttemptPolicyStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newLegacyAttemptPolicyManager(store: LegacyAttemptPolicyStore, clock: Clock): LegacyAttemptPolicyManager {
    return LegacyAttemptPolicyManager(
        store = store,
        validator = defaultLegacyAttemptPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
