package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.linehaul.clock.Clock

/** Moves batched backhaul assignments through their life cycle and keeps a journal of what it did. */
data class BatchedBackhaulAssignmentDesk(
    var store: BatchedBackhaulAssignmentStore,
    var validator: BatchedBackhaulAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched backhaul assignment $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedBackhaulAssignmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched backhaul assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedBackhaulAssignmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newBatchedBackhaulAssignmentDesk(store: BatchedBackhaulAssignmentStore, clock: Clock): BatchedBackhaulAssignmentDesk {
    return BatchedBackhaulAssignmentDesk(
        store = store,
        validator = defaultBatchedBackhaulAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
