package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves default emission factor adjustments through their life cycle and keeps a journal of what it did.
 */
data class DefaultEmissionFactorAdjustmentDesk(
    var store: DefaultEmissionFactorAdjustmentStore,
    var validator: DefaultEmissionFactorAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultEmissionFactorAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default emission factor adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultEmissionFactorAdjustmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default emission factor adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultEmissionFactorAdjustmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default emission factor adjustment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDefaultEmissionFactorAdjustmentDesk(store: DefaultEmissionFactorAdjustmentStore, clock: Clock, flags: Registry): DefaultEmissionFactorAdjustmentDesk {
    return DefaultEmissionFactorAdjustmentDesk(
        store = store,
        validator = defaultDefaultEmissionFactorAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultEmissionFactorAdjustmentEngine(flags),
    )
}
