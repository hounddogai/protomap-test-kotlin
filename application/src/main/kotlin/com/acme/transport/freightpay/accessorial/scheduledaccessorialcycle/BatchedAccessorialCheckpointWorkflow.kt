package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves batched accessorial checkpoints through their life cycle and keeps a journal of what it did.
 */
data class BatchedAccessorialCheckpointCoordinator(
    var store: BatchedAccessorialCheckpointStore,
    var validator: BatchedAccessorialCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched accessorial checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAccessorialCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched accessorial checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAccessorialCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newBatchedAccessorialCheckpointCoordinator(store: BatchedAccessorialCheckpointStore, clock: Clock): BatchedAccessorialCheckpointCoordinator {
    return BatchedAccessorialCheckpointCoordinator(
        store = store,
        validator = defaultBatchedAccessorialCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
