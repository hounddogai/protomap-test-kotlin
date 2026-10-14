package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor variances through their life cycle and keeps a journal of what it did. */
data class EmissionFactorVarianceManager(
    var store: EmissionFactorVarianceStore,
    var validator: EmissionFactorVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("emission factor variance $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newEmissionFactorVarianceManager(store: EmissionFactorVarianceStore, clock: Clock): EmissionFactorVarianceManager {
    return EmissionFactorVarianceManager(
        store = store,
        validator = defaultEmissionFactorVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
