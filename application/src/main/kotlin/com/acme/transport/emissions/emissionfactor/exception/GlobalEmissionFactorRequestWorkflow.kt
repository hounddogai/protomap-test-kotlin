package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves global emission factor requests through their life cycle and keeps a journal of what it did.
 */
data class GlobalEmissionFactorRequestWorkflow(
    var store: GlobalEmissionFactorRequestStore,
    var validator: GlobalEmissionFactorRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalEmissionFactorRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global emission factor request $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalEmissionFactorRequestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global emission factor request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalEmissionFactorRequestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global emission factor request $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newGlobalEmissionFactorRequestWorkflow(store: GlobalEmissionFactorRequestStore, clock: Clock, flags: Registry): GlobalEmissionFactorRequestWorkflow {
    return GlobalEmissionFactorRequestWorkflow(
        store = store,
        validator = defaultGlobalEmissionFactorRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalEmissionFactorRequestEngine(flags),
    )
}
