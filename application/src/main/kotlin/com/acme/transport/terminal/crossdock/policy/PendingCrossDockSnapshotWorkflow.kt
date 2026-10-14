package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.terminal.clock.Clock

/** Moves pending cross dock snapshots through their life cycle and keeps a journal of what it did. */
data class PendingCrossDockSnapshotWorkflow(
    var store: PendingCrossDockSnapshotStore,
    var validator: PendingCrossDockSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending cross dock snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCrossDockSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending cross dock snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCrossDockSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newPendingCrossDockSnapshotWorkflow(store: PendingCrossDockSnapshotStore, clock: Clock): PendingCrossDockSnapshotWorkflow {
    return PendingCrossDockSnapshotWorkflow(
        store = store,
        validator = defaultPendingCrossDockSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
