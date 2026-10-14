package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves batched zone variances through their life cycle and keeps a journal of what it did. */
data class BatchedZoneVarianceManager(
    var store: BatchedZoneVarianceStore,
    var validator: BatchedZoneVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedZoneVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched zone variance $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedZoneVarianceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched zone variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedZoneVarianceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched zone variance $code not found")
        val result = engine.price(item)
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

fun newBatchedZoneVarianceManager(store: BatchedZoneVarianceStore, clock: Clock, flags: Registry): BatchedZoneVarianceManager {
    return BatchedZoneVarianceManager(
        store = store,
        validator = defaultBatchedZoneVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedZoneVarianceEngine(flags),
    )
}
