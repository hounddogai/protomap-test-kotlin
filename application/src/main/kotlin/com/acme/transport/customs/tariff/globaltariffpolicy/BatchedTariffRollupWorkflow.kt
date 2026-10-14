package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.clock.Clock

/** Moves batched tariff rollups through their life cycle and keeps a journal of what it did. */
data class BatchedTariffRollupWorkflow(
    var store: BatchedTariffRollupStore,
    var validator: BatchedTariffRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedTariffRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched tariff rollup $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTariffRollupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched tariff rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTariffRollupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched tariff rollup $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newBatchedTariffRollupWorkflow(store: BatchedTariffRollupStore, clock: Clock): BatchedTariffRollupWorkflow {
    return BatchedTariffRollupWorkflow(
        store = store,
        validator = defaultBatchedTariffRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedTariffRollupEngine(),
    )
}
