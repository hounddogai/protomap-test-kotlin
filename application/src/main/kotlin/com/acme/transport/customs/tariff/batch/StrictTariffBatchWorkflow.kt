package com.acme.transport.customs.tariff.batch

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict tariff batches through their life cycle and keeps a journal of what it did. */
data class StrictTariffBatchWorkflow(
    var store: StrictTariffBatchStore,
    var validator: StrictTariffBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictTariffBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict tariff batch $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTariffBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict tariff batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTariffBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict tariff batch $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newStrictTariffBatchWorkflow(store: StrictTariffBatchStore, clock: Clock, flags: Registry): StrictTariffBatchWorkflow {
    return StrictTariffBatchWorkflow(
        store = store,
        validator = defaultStrictTariffBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictTariffBatchEngine(flags),
    )
}
