package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.clock.Clock

/** Moves batched tariff digests through their life cycle and keeps a journal of what it did. */
data class BatchedTariffDigestCoordinator(
    var store: BatchedTariffDigestStore,
    var validator: BatchedTariffDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched tariff digest $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTariffDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched tariff digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTariffDigestStatus.CANCELLED)
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

fun newBatchedTariffDigestCoordinator(store: BatchedTariffDigestStore, clock: Clock): BatchedTariffDigestCoordinator {
    return BatchedTariffDigestCoordinator(
        store = store,
        validator = defaultBatchedTariffDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
