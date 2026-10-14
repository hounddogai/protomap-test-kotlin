package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.emissions.clock.Clock

/**
 * Moves weighted emission factor batches through their life cycle and keeps a journal of what it did.
 */
data class WeightedEmissionFactorBatchManager(
    var store: WeightedEmissionFactorBatchStore,
    var validator: WeightedEmissionFactorBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted emission factor batch $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedEmissionFactorBatchStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted emission factor batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedEmissionFactorBatchStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newWeightedEmissionFactorBatchManager(store: WeightedEmissionFactorBatchStore, clock: Clock): WeightedEmissionFactorBatchManager {
    return WeightedEmissionFactorBatchManager(
        store = store,
        validator = defaultWeightedEmissionFactorBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
