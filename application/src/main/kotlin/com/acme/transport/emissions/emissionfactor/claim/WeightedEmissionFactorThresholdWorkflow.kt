package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves weighted emission factor thresholds through their life cycle and keeps a journal of what it did.
 */
data class WeightedEmissionFactorThresholdWorkflow(
    var store: WeightedEmissionFactorThresholdStore,
    var validator: WeightedEmissionFactorThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedEmissionFactorThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted emission factor threshold $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedEmissionFactorThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted emission factor threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedEmissionFactorThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted emission factor threshold $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newWeightedEmissionFactorThresholdWorkflow(store: WeightedEmissionFactorThresholdStore, clock: Clock, flags: Registry): WeightedEmissionFactorThresholdWorkflow {
    return WeightedEmissionFactorThresholdWorkflow(
        store = store,
        validator = defaultWeightedEmissionFactorThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedEmissionFactorThresholdEngine(flags),
    )
}
