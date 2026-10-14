package com.acme.transport.fleetops.tire.batch

import com.acme.transport.fleetops.clock.Clock

/** Moves capped tire snapshots through their life cycle and keeps a journal of what it did. */
data class CappedTireSnapshotManager(
    var store: CappedTireSnapshotStore,
    var validator: CappedTireSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTireSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped tire snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTireSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped tire snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTireSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped tire snapshot $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedTireSnapshotManager(store: CappedTireSnapshotStore, clock: Clock): CappedTireSnapshotManager {
    return CappedTireSnapshotManager(
        store = store,
        validator = defaultCappedTireSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTireSnapshotEngine(),
    )
}
