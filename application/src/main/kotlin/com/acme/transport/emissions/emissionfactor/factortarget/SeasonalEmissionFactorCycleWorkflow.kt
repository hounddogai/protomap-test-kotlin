package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.clock.Clock

/**
 * Moves seasonal emission factor cycles through their life cycle and keeps a journal of what it did.
 */
data class SeasonalEmissionFactorCycleManager(
    var store: SeasonalEmissionFactorCycleStore,
    var validator: SeasonalEmissionFactorCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal emission factor cycle $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalEmissionFactorCycleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal emission factor cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalEmissionFactorCycleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newSeasonalEmissionFactorCycleManager(store: SeasonalEmissionFactorCycleStore, clock: Clock): SeasonalEmissionFactorCycleManager {
    return SeasonalEmissionFactorCycleManager(
        store = store,
        validator = defaultSeasonalEmissionFactorCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
