package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor cycles through their life cycle and keeps a journal of what it did. */
data class EmissionFactorCycleCoordinator(
    var store: EmissionFactorCycleStore,
    var validator: EmissionFactorCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor cycle $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorCycleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorCycleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("emission factor cycle $code not found")
        val result = engine.evaluate(item)
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

fun newEmissionFactorCycleCoordinator(store: EmissionFactorCycleStore, clock: Clock): EmissionFactorCycleCoordinator {
    return EmissionFactorCycleCoordinator(
        store = store,
        validator = defaultEmissionFactorCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorCycleEngine(),
    )
}
