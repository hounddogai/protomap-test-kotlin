package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.terminal.clock.Clock

/** Moves dock door records through their life cycle and keeps a journal of what it did. */
data class DockDoorRecordCoordinator(
    var store: DockDoorRecordStore,
    var validator: DockDoorRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dock door record $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDockDoorRecordCoordinator(store: DockDoorRecordStore, clock: Clock): DockDoorRecordCoordinator {
    return DockDoorRecordCoordinator(
        store = store,
        validator = defaultDockDoorRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
