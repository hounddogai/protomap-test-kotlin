package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor adjustments through their life cycle and keeps a journal of what it did. */
data class EmissionFactorAdjustmentService(
    var store: EmissionFactorAdjustmentStore,
    var validator: EmissionFactorAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("emission factor adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("emission factor adjustment $code not found")
        val result = engine.price(item)
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

fun newEmissionFactorAdjustmentService(store: EmissionFactorAdjustmentStore, clock: Clock, flags: Registry): EmissionFactorAdjustmentService {
    return EmissionFactorAdjustmentService(
        store = store,
        validator = defaultEmissionFactorAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorAdjustmentEngine(flags),
    )
}
