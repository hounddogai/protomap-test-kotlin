package com.acme.transport.crewing.roster.plan

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched roster mappings through their life cycle and keeps a journal of what it did. */
data class BatchedRosterMappingCoordinator(
    var store: BatchedRosterMappingStore,
    var validator: BatchedRosterMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedRosterMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched roster mapping $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedRosterMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched roster mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedRosterMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched roster mapping $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newBatchedRosterMappingCoordinator(store: BatchedRosterMappingStore, clock: Clock, flags: Registry): BatchedRosterMappingCoordinator {
    return BatchedRosterMappingCoordinator(
        store = store,
        validator = defaultBatchedRosterMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedRosterMappingEngine(flags),
    )
}
