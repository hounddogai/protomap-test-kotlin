package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.network.clock.Clock

/**
 * Moves batched service area snapshots through their life cycle and keeps a journal of what it did.
 */
data class BatchedServiceAreaSnapshotWorkflow(
    var store: BatchedServiceAreaSnapshotStore,
    var validator: BatchedServiceAreaSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched service area snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedServiceAreaSnapshotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched service area snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedServiceAreaSnapshotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newBatchedServiceAreaSnapshotWorkflow(store: BatchedServiceAreaSnapshotStore, clock: Clock): BatchedServiceAreaSnapshotWorkflow {
    return BatchedServiceAreaSnapshotWorkflow(
        store = store,
        validator = defaultBatchedServiceAreaSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
