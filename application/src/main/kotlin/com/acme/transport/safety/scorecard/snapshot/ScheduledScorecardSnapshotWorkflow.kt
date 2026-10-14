package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.safety.clock.Clock

/** Moves scheduled scorecard snapshots through their life cycle and keeps a journal of what it did. */
data class ScheduledScorecardSnapshotManager(
    var store: ScheduledScorecardSnapshotStore,
    var validator: ScheduledScorecardSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledScorecardSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled scorecard snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledScorecardSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled scorecard snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledScorecardSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled scorecard snapshot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledScorecardSnapshotManager(store: ScheduledScorecardSnapshotStore, clock: Clock): ScheduledScorecardSnapshotManager {
    return ScheduledScorecardSnapshotManager(
        store = store,
        validator = defaultScheduledScorecardSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledScorecardSnapshotEngine(),
    )
}
