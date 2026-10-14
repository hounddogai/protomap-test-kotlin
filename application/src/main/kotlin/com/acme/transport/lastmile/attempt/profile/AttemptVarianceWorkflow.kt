package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.lastmile.clock.Clock

/** Moves attempt variances through their life cycle and keeps a journal of what it did. */
data class AttemptVarianceManager(
    var store: AttemptVarianceStore,
    var validator: AttemptVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("attempt variance $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newAttemptVarianceManager(store: AttemptVarianceStore, clock: Clock): AttemptVarianceManager {
    return AttemptVarianceManager(
        store = store,
        validator = defaultAttemptVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
