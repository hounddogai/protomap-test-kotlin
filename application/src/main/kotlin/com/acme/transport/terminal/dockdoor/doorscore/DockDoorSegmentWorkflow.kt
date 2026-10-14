package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.terminal.clock.Clock

/** Moves dock door segments through their life cycle and keeps a journal of what it did. */
data class DockDoorSegmentWorkflow(
    var store: DockDoorSegmentStore,
    var validator: DockDoorSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dock door segment $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorSegmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorSegmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newDockDoorSegmentWorkflow(store: DockDoorSegmentStore, clock: Clock): DockDoorSegmentWorkflow {
    return DockDoorSegmentWorkflow(
        store = store,
        validator = defaultDockDoorSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
