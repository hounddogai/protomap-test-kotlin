package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves batched transit time requests through their life cycle and keeps a journal of what it did. */
data class BatchedTransitTimeRequestCoordinator(
    var store: BatchedTransitTimeRequestStore,
    var validator: BatchedTransitTimeRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedTransitTimeRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched transit time request $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTransitTimeRequestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched transit time request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTransitTimeRequestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched transit time request $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newBatchedTransitTimeRequestCoordinator(store: BatchedTransitTimeRequestStore, clock: Clock, flags: Registry): BatchedTransitTimeRequestCoordinator {
    return BatchedTransitTimeRequestCoordinator(
        store = store,
        validator = defaultBatchedTransitTimeRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedTransitTimeRequestEngine(flags),
    )
}
