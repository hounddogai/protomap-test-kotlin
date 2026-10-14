package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves roster snapshots through their life cycle and keeps a journal of what it did. */
data class RosterSnapshotDesk(
    var store: RosterSnapshotStore,
    var validator: RosterSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RosterSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("roster snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RosterSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("roster snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("roster snapshot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newRosterSnapshotDesk(store: RosterSnapshotStore, clock: Clock, flags: Registry): RosterSnapshotDesk {
    return RosterSnapshotDesk(
        store = store,
        validator = defaultRosterSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRosterSnapshotEngine(flags),
    )
}
