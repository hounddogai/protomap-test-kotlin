package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.clock.Clock

/**
 * Moves fallback emission factor batches through their life cycle and keeps a journal of what it did.
 */
data class FallbackEmissionFactorBatchCoordinator(
    var store: FallbackEmissionFactorBatchStore,
    var validator: FallbackEmissionFactorBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback emission factor batch $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEmissionFactorBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback emission factor batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEmissionFactorBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newFallbackEmissionFactorBatchCoordinator(store: FallbackEmissionFactorBatchStore, clock: Clock): FallbackEmissionFactorBatchCoordinator {
    return FallbackEmissionFactorBatchCoordinator(
        store = store,
        validator = defaultFallbackEmissionFactorBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
