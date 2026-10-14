package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.terminal.clock.Clock

/** Moves batched dock door records through their life cycle and keeps a journal of what it did. */
data class BatchedDockDoorRecordService(
    var store: BatchedDockDoorRecordStore,
    var validator: BatchedDockDoorRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched dock door record $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDockDoorRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched dock door record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDockDoorRecordStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newBatchedDockDoorRecordService(store: BatchedDockDoorRecordStore, clock: Clock): BatchedDockDoorRecordService {
    return BatchedDockDoorRecordService(
        store = store,
        validator = defaultBatchedDockDoorRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
