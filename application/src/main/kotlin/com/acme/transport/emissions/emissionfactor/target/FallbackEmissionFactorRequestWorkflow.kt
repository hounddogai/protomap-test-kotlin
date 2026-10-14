package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback emission factor requests through their life cycle and keeps a journal of what it did.
 */
data class FallbackEmissionFactorRequestWorkflow(
    var store: FallbackEmissionFactorRequestStore,
    var validator: FallbackEmissionFactorRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackEmissionFactorRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback emission factor request $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEmissionFactorRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback emission factor request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEmissionFactorRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback emission factor request $code not found")
        val result = engine.compute(item)
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

fun newFallbackEmissionFactorRequestWorkflow(store: FallbackEmissionFactorRequestStore, clock: Clock, flags: Registry): FallbackEmissionFactorRequestWorkflow {
    return FallbackEmissionFactorRequestWorkflow(
        store = store,
        validator = defaultFallbackEmissionFactorRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackEmissionFactorRequestEngine(flags),
    )
}
