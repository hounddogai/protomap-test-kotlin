package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.terminal.clock.Clock

/** Moves batched dock door checkpoints through their life cycle and keeps a journal of what it did. */
data class BatchedDockDoorCheckpointDesk(
    var store: BatchedDockDoorCheckpointStore,
    var validator: BatchedDockDoorCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched dock door checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDockDoorCheckpointStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched dock door checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDockDoorCheckpointStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newBatchedDockDoorCheckpointDesk(store: BatchedDockDoorCheckpointStore, clock: Clock): BatchedDockDoorCheckpointDesk {
    return BatchedDockDoorCheckpointDesk(
        store = store,
        validator = defaultBatchedDockDoorCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
