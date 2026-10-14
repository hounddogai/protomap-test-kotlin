package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.lastmile.clock.Clock

/** Moves batched locker adjustments through their life cycle and keeps a journal of what it did. */
data class BatchedLockerAdjustmentDesk(
    var store: BatchedLockerAdjustmentStore,
    var validator: BatchedLockerAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched locker adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedLockerAdjustmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched locker adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedLockerAdjustmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newBatchedLockerAdjustmentDesk(store: BatchedLockerAdjustmentStore, clock: Clock): BatchedLockerAdjustmentDesk {
    return BatchedLockerAdjustmentDesk(
        store = store,
        validator = defaultBatchedLockerAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
