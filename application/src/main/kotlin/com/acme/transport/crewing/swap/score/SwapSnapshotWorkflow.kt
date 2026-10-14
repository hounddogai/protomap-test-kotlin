package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.clock.Clock

/** Moves swap snapshots through their life cycle and keeps a journal of what it did. */
data class SwapSnapshotWorkflow(
    var store: SwapSnapshotStore,
    var validator: SwapSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("swap snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(SwapSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("swap snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newSwapSnapshotWorkflow(store: SwapSnapshotStore, clock: Clock): SwapSnapshotWorkflow {
    return SwapSnapshotWorkflow(
        store = store,
        validator = defaultSwapSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
