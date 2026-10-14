package com.acme.transport.linehaul.capacity.target

import com.acme.transport.linehaul.clock.Clock

/** Moves batched capacity agreements through their life cycle and keeps a journal of what it did. */
data class BatchedCapacityAgreementCoordinator(
    var store: BatchedCapacityAgreementStore,
    var validator: BatchedCapacityAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched capacity agreement $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedCapacityAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched capacity agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedCapacityAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newBatchedCapacityAgreementCoordinator(store: BatchedCapacityAgreementStore, clock: Clock): BatchedCapacityAgreementCoordinator {
    return BatchedCapacityAgreementCoordinator(
        store = store,
        validator = defaultBatchedCapacityAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
