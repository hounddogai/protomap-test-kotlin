package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor overrides through their life cycle and keeps a journal of what it did. */
data class EmissionFactorOverrideManager(
    var store: EmissionFactorOverrideStore,
    var validator: EmissionFactorOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor override $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("emission factor override $code not found")
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

fun newEmissionFactorOverrideManager(store: EmissionFactorOverrideStore, clock: Clock, flags: Registry): EmissionFactorOverrideManager {
    return EmissionFactorOverrideManager(
        store = store,
        validator = defaultEmissionFactorOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorOverrideEngine(flags),
    )
}
