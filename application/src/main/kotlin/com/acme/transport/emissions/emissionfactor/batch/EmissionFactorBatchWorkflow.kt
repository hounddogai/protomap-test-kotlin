package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor batches through their life cycle and keeps a journal of what it did. */
data class EmissionFactorBatchCoordinator(
    var store: EmissionFactorBatchStore,
    var validator: EmissionFactorBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor batch $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorBatchStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorBatchStatus.STOPPED)
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

fun newEmissionFactorBatchCoordinator(store: EmissionFactorBatchStore, clock: Clock): EmissionFactorBatchCoordinator {
    return EmissionFactorBatchCoordinator(
        store = store,
        validator = defaultEmissionFactorBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
