package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves batched tender forecasts through their life cycle and keeps a journal of what it did. */
data class BatchedTenderForecastCoordinator(
    var store: BatchedTenderForecastStore,
    var validator: BatchedTenderForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedTenderForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched tender forecast $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedTenderForecastStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched tender forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedTenderForecastStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched tender forecast $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newBatchedTenderForecastCoordinator(store: BatchedTenderForecastStore, clock: Clock, flags: Registry): BatchedTenderForecastCoordinator {
    return BatchedTenderForecastCoordinator(
        store = store,
        validator = defaultBatchedTenderForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedTenderForecastEngine(flags),
    )
}
