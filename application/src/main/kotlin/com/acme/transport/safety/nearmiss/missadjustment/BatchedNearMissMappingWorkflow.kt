package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves batched near miss mappings through their life cycle and keeps a journal of what it did. */
data class BatchedNearMissMappingCoordinator(
    var store: BatchedNearMissMappingStore,
    var validator: BatchedNearMissMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedNearMissMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched near miss mapping $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedNearMissMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched near miss mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedNearMissMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched near miss mapping $code not found")
        val result = engine.evaluate(item)
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

fun newBatchedNearMissMappingCoordinator(store: BatchedNearMissMappingStore, clock: Clock, flags: Registry): BatchedNearMissMappingCoordinator {
    return BatchedNearMissMappingCoordinator(
        store = store,
        validator = defaultBatchedNearMissMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedNearMissMappingEngine(flags),
    )
}
