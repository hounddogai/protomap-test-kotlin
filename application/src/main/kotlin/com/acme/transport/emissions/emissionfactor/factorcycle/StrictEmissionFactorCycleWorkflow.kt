package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.clock.Clock

/** Moves strict emission factor cycles through their life cycle and keeps a journal of what it did. */
data class StrictEmissionFactorCycleManager(
    var store: StrictEmissionFactorCycleStore,
    var validator: StrictEmissionFactorCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict emission factor cycle $code not found")
        validator.requireValid(item)
        item.moveTo(StrictEmissionFactorCycleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict emission factor cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictEmissionFactorCycleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newStrictEmissionFactorCycleManager(store: StrictEmissionFactorCycleStore, clock: Clock): StrictEmissionFactorCycleManager {
    return StrictEmissionFactorCycleManager(
        store = store,
        validator = defaultStrictEmissionFactorCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
