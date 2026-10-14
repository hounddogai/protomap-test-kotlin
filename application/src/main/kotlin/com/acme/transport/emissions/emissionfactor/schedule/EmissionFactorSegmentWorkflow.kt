package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor segments through their life cycle and keeps a journal of what it did. */
data class EmissionFactorSegmentService(
    var store: EmissionFactorSegmentStore,
    var validator: EmissionFactorSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("emission factor segment $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("emission factor segment $code not found")
        val result = engine.evaluate(item)
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

fun newEmissionFactorSegmentService(store: EmissionFactorSegmentStore, clock: Clock, flags: Registry): EmissionFactorSegmentService {
    return EmissionFactorSegmentService(
        store = store,
        validator = defaultEmissionFactorSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorSegmentEngine(flags),
    )
}
