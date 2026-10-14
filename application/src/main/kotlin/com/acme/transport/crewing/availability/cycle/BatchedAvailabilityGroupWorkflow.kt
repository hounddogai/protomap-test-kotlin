package com.acme.transport.crewing.availability.cycle

import com.acme.transport.crewing.clock.Clock

/** Moves batched availability groups through their life cycle and keeps a journal of what it did. */
data class BatchedAvailabilityGroupManager(
    var store: BatchedAvailabilityGroupStore,
    var validator: BatchedAvailabilityGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAvailabilityGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched availability group $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAvailabilityGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched availability group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAvailabilityGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched availability group $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newBatchedAvailabilityGroupManager(store: BatchedAvailabilityGroupStore, clock: Clock): BatchedAvailabilityGroupManager {
    return BatchedAvailabilityGroupManager(
        store = store,
        validator = defaultBatchedAvailabilityGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAvailabilityGroupEngine(),
    )
}
