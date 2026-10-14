package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.clock.Clock

/** Moves batched route offset mappings through their life cycle and keeps a journal of what it did. */
data class BatchedRouteOffsetMappingDesk(
    var store: BatchedRouteOffsetMappingStore,
    var validator: BatchedRouteOffsetMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched route offset mapping $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedRouteOffsetMappingStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched route offset mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedRouteOffsetMappingStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newBatchedRouteOffsetMappingDesk(store: BatchedRouteOffsetMappingStore, clock: Clock): BatchedRouteOffsetMappingDesk {
    return BatchedRouteOffsetMappingDesk(
        store = store,
        validator = defaultBatchedRouteOffsetMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
