package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.crewing.clock.Clock

/** Moves pending swap snapshots through their life cycle and keeps a journal of what it did. */
data class PendingSwapSnapshotWorkflow(
    var store: PendingSwapSnapshotStore,
    var validator: PendingSwapSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending swap snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PendingSwapSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending swap snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingSwapSnapshotStatus.RETIRED)
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

fun newPendingSwapSnapshotWorkflow(store: PendingSwapSnapshotStore, clock: Clock): PendingSwapSnapshotWorkflow {
    return PendingSwapSnapshotWorkflow(
        store = store,
        validator = defaultPendingSwapSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
