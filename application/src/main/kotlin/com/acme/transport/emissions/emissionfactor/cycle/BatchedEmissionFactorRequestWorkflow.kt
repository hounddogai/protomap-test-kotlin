package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves batched emission factor requests through their life cycle and keeps a journal of what it did.
 */
data class BatchedEmissionFactorRequestWorkflow(
    var store: BatchedEmissionFactorRequestStore,
    var validator: BatchedEmissionFactorRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedEmissionFactorRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched emission factor request $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedEmissionFactorRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched emission factor request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedEmissionFactorRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched emission factor request $code not found")
        val result = engine.compute(item)
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

fun newBatchedEmissionFactorRequestWorkflow(store: BatchedEmissionFactorRequestStore, clock: Clock, flags: Registry): BatchedEmissionFactorRequestWorkflow {
    return BatchedEmissionFactorRequestWorkflow(
        store = store,
        validator = defaultBatchedEmissionFactorRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedEmissionFactorRequestEngine(flags),
    )
}
