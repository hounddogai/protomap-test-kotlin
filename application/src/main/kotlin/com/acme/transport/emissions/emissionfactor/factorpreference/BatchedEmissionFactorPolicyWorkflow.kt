package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.clock.Clock

/**
 * Moves batched emission factor policies through their life cycle and keeps a journal of what it did.
 */
data class BatchedEmissionFactorPolicyCoordinator(
    var store: BatchedEmissionFactorPolicyStore,
    var validator: BatchedEmissionFactorPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched emission factor policy $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedEmissionFactorPolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched emission factor policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedEmissionFactorPolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newBatchedEmissionFactorPolicyCoordinator(store: BatchedEmissionFactorPolicyStore, clock: Clock): BatchedEmissionFactorPolicyCoordinator {
    return BatchedEmissionFactorPolicyCoordinator(
        store = store,
        validator = defaultBatchedEmissionFactorPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
