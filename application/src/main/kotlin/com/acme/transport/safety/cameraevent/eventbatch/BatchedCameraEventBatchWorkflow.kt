package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.safety.clock.Clock

/** Moves batched camera event batches through their life cycle and keeps a journal of what it did. */
data class BatchedCameraEventBatchCoordinator(
    var store: BatchedCameraEventBatchStore,
    var validator: BatchedCameraEventBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched camera event batch $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedCameraEventBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched camera event batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedCameraEventBatchStatus.CLOSED)
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

fun newBatchedCameraEventBatchCoordinator(store: BatchedCameraEventBatchStore, clock: Clock): BatchedCameraEventBatchCoordinator {
    return BatchedCameraEventBatchCoordinator(
        store = store,
        validator = defaultBatchedCameraEventBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
