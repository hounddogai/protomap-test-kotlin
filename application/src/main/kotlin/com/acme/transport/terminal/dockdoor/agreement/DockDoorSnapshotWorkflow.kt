package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.terminal.clock.Clock

/** Moves dock door snapshots through their life cycle and keeps a journal of what it did. */
data class DockDoorSnapshotDesk(
    var store: DockDoorSnapshotStore,
    var validator: DockDoorSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dock door snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDockDoorSnapshotDesk(store: DockDoorSnapshotStore, clock: Clock): DockDoorSnapshotDesk {
    return DockDoorSnapshotDesk(
        store = store,
        validator = defaultDockDoorSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
