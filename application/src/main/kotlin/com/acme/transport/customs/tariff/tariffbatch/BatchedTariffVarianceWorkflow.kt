package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched tariff variances through their life cycle and keeps a journal of what it did. */
data class BatchedTariffVarianceManager(
    var store: BatchedTariffVarianceStore,
    var validator: BatchedTariffVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedTariffVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched tariff variance $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTariffVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched tariff variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTariffVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched tariff variance $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newBatchedTariffVarianceManager(store: BatchedTariffVarianceStore, clock: Clock, flags: Registry): BatchedTariffVarianceManager {
    return BatchedTariffVarianceManager(
        store = store,
        validator = defaultBatchedTariffVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedTariffVarianceEngine(flags),
    )
}
