package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves scorecard snapshots through their life cycle and keeps a journal of what it did. */
data class ScorecardSnapshotManager(
    var store: ScorecardSnapshotStore,
    var validator: ScorecardSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScorecardSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scorecard snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardSnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardSnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scorecard snapshot $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newScorecardSnapshotManager(store: ScorecardSnapshotStore, clock: Clock, flags: Registry): ScorecardSnapshotManager {
    return ScorecardSnapshotManager(
        store = store,
        validator = defaultScorecardSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScorecardSnapshotEngine(flags),
    )
}
