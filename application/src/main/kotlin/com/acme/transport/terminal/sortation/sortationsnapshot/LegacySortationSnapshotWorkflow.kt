package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.terminal.clock.Clock

/** Moves legacy sortation snapshots through their life cycle and keeps a journal of what it did. */
data class LegacySortationSnapshotCoordinator(
    var store: LegacySortationSnapshotStore,
    var validator: LegacySortationSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy sortation snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacySortationSnapshotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy sortation snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacySortationSnapshotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLegacySortationSnapshotCoordinator(store: LegacySortationSnapshotStore, clock: Clock): LegacySortationSnapshotCoordinator {
    return LegacySortationSnapshotCoordinator(
        store = store,
        validator = defaultLegacySortationSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
