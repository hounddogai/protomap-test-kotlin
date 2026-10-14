package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.clock.Clock

/** Moves batched defect mappings through their life cycle and keeps a journal of what it did. */
data class BatchedDefectMappingCoordinator(
    var store: BatchedDefectMappingStore,
    var validator: BatchedDefectMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched defect mapping $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDefectMappingStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched defect mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDefectMappingStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newBatchedDefectMappingCoordinator(store: BatchedDefectMappingStore, clock: Clock): BatchedDefectMappingCoordinator {
    return BatchedDefectMappingCoordinator(
        store = store,
        validator = defaultBatchedDefectMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
