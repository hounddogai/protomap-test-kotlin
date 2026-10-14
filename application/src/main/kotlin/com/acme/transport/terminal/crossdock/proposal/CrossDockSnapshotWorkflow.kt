package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock snapshots through their life cycle and keeps a journal of what it did. */
data class CrossDockSnapshotWorkflow(
    var store: CrossDockSnapshotStore,
    var validator: CrossDockSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cross dock snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockSnapshotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockSnapshotStatus.WITHDRAWN)
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

fun newCrossDockSnapshotWorkflow(store: CrossDockSnapshotStore, clock: Clock): CrossDockSnapshotWorkflow {
    return CrossDockSnapshotWorkflow(
        store = store,
        validator = defaultCrossDockSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
