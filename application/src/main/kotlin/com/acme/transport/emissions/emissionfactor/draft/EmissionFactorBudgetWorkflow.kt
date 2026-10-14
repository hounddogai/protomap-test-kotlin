package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor budgets through their life cycle and keeps a journal of what it did. */
data class EmissionFactorBudgetCoordinator(
    var store: EmissionFactorBudgetStore,
    var validator: EmissionFactorBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor budget $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorBudgetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorBudgetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("emission factor budget $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newEmissionFactorBudgetCoordinator(store: EmissionFactorBudgetStore, clock: Clock, flags: Registry): EmissionFactorBudgetCoordinator {
    return EmissionFactorBudgetCoordinator(
        store = store,
        validator = defaultEmissionFactorBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorBudgetEngine(flags),
    )
}
