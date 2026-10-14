package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal tender batches through their life cycle and keeps a journal of what it did. */
data class SeasonalTenderBatchCoordinator(
    var store: SeasonalTenderBatchStore,
    var validator: SeasonalTenderBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTenderBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal tender batch $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTenderBatchStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal tender batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTenderBatchStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal tender batch $code not found")
        val result = engine.compute(item)
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

fun newSeasonalTenderBatchCoordinator(store: SeasonalTenderBatchStore, clock: Clock, flags: Registry): SeasonalTenderBatchCoordinator {
    return SeasonalTenderBatchCoordinator(
        store = store,
        validator = defaultSeasonalTenderBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTenderBatchEngine(flags),
    )
}
