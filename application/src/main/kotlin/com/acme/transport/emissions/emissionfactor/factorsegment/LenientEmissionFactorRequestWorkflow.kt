package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.emissions.clock.Clock

/**
 * Moves lenient emission factor requests through their life cycle and keeps a journal of what it did.
 */
data class LenientEmissionFactorRequestWorkflow(
    var store: LenientEmissionFactorRequestStore,
    var validator: LenientEmissionFactorRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientEmissionFactorRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient emission factor request $code not found")
        validator.requireValid(item)
        item.moveTo(LenientEmissionFactorRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient emission factor request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientEmissionFactorRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient emission factor request $code not found")
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

fun newLenientEmissionFactorRequestWorkflow(store: LenientEmissionFactorRequestStore, clock: Clock): LenientEmissionFactorRequestWorkflow {
    return LenientEmissionFactorRequestWorkflow(
        store = store,
        validator = defaultLenientEmissionFactorRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientEmissionFactorRequestEngine(),
    )
}
