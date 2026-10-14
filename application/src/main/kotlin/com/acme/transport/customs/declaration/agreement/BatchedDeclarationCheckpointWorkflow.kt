package com.acme.transport.customs.declaration.agreement

import com.acme.transport.customs.clock.Clock

/**
 * Moves batched declaration checkpoints through their life cycle and keeps a journal of what it did.
 */
data class BatchedDeclarationCheckpointManager(
    var store: BatchedDeclarationCheckpointStore,
    var validator: BatchedDeclarationCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched declaration checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDeclarationCheckpointStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched declaration checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDeclarationCheckpointStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newBatchedDeclarationCheckpointManager(store: BatchedDeclarationCheckpointStore, clock: Clock): BatchedDeclarationCheckpointManager {
    return BatchedDeclarationCheckpointManager(
        store = store,
        validator = defaultBatchedDeclarationCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
