package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.lastmile.clock.Clock

/** Moves rolling attempt thresholds through their life cycle and keeps a journal of what it did. */
data class RollingAttemptThresholdService(
    var store: RollingAttemptThresholdStore,
    var validator: RollingAttemptThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling attempt threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RollingAttemptThresholdStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling attempt threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingAttemptThresholdStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRollingAttemptThresholdService(store: RollingAttemptThresholdStore, clock: Clock): RollingAttemptThresholdService {
    return RollingAttemptThresholdService(
        store = store,
        validator = defaultRollingAttemptThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
