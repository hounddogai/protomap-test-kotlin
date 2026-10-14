package com.acme.transport.safety.coaching.snapshot

import com.acme.transport.safety.clock.Clock

/** Moves coaching snapshots through their life cycle and keeps a journal of what it did. */
data class CoachingSnapshotManager(
    var store: CoachingSnapshotStore,
    var validator: CoachingSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("coaching snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingSnapshotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingSnapshotStatus.WITHDRAWN)
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

fun newCoachingSnapshotManager(store: CoachingSnapshotStore, clock: Clock): CoachingSnapshotManager {
    return CoachingSnapshotManager(
        store = store,
        validator = defaultCoachingSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
