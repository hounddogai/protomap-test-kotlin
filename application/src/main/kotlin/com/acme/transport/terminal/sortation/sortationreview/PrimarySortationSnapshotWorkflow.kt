package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.terminal.clock.Clock

/** Moves primary sortation snapshots through their life cycle and keeps a journal of what it did. */
data class PrimarySortationSnapshotService(
    var store: PrimarySortationSnapshotStore,
    var validator: PrimarySortationSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary sortation snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PrimarySortationSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary sortation snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimarySortationSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newPrimarySortationSnapshotService(store: PrimarySortationSnapshotStore, clock: Clock): PrimarySortationSnapshotService {
    return PrimarySortationSnapshotService(
        store = store,
        validator = defaultPrimarySortationSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
