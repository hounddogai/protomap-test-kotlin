package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves weighted emission factor templates through their life cycle and keeps a journal of what it did.
 */
data class WeightedEmissionFactorTemplateDesk(
    var store: WeightedEmissionFactorTemplateStore,
    var validator: WeightedEmissionFactorTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedEmissionFactorTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted emission factor template $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedEmissionFactorTemplateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted emission factor template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedEmissionFactorTemplateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted emission factor template $code not found")
        val result = engine.run(item)
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

fun newWeightedEmissionFactorTemplateDesk(store: WeightedEmissionFactorTemplateStore, clock: Clock, flags: Registry): WeightedEmissionFactorTemplateDesk {
    return WeightedEmissionFactorTemplateDesk(
        store = store,
        validator = defaultWeightedEmissionFactorTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedEmissionFactorTemplateEngine(flags),
    )
}
