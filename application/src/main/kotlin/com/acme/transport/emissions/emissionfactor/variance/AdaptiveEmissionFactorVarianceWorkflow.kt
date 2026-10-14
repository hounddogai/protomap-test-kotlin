package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.emissions.clock.Clock

/**
 * Moves adaptive emission factor variances through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveEmissionFactorVarianceCoordinator(
    var store: AdaptiveEmissionFactorVarianceStore,
    var validator: AdaptiveEmissionFactorVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive emission factor variance $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveEmissionFactorVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive emission factor variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveEmissionFactorVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newAdaptiveEmissionFactorVarianceCoordinator(store: AdaptiveEmissionFactorVarianceStore, clock: Clock): AdaptiveEmissionFactorVarianceCoordinator {
    return AdaptiveEmissionFactorVarianceCoordinator(
        store = store,
        validator = defaultAdaptiveEmissionFactorVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
