package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.lastmile.clock.Clock

/** Moves rolling attempt snapshots through their life cycle and keeps a journal of what it did. */
data class RollingAttemptSnapshotCoordinator(
    var store: RollingAttemptSnapshotStore,
    var validator: RollingAttemptSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling attempt snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RollingAttemptSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling attempt snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingAttemptSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newRollingAttemptSnapshotCoordinator(store: RollingAttemptSnapshotStore, clock: Clock): RollingAttemptSnapshotCoordinator {
    return RollingAttemptSnapshotCoordinator(
        store = store,
        validator = defaultRollingAttemptSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
