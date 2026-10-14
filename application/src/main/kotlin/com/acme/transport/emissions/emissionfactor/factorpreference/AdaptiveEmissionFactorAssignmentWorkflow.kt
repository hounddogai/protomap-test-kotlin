package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves adaptive emission factor assignments through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveEmissionFactorAssignmentService(
    var store: AdaptiveEmissionFactorAssignmentStore,
    var validator: AdaptiveEmissionFactorAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveEmissionFactorAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive emission factor assignment $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveEmissionFactorAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive emission factor assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveEmissionFactorAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive emission factor assignment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newAdaptiveEmissionFactorAssignmentService(store: AdaptiveEmissionFactorAssignmentStore, clock: Clock, flags: Registry): AdaptiveEmissionFactorAssignmentService {
    return AdaptiveEmissionFactorAssignmentService(
        store = store,
        validator = defaultAdaptiveEmissionFactorAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveEmissionFactorAssignmentEngine(flags),
    )
}
