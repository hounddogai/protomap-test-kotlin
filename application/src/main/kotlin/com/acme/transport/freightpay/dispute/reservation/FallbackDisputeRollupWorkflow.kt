package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback dispute rollups through their life cycle and keeps a journal of what it did. */
data class FallbackDisputeRollupManager(
    var store: FallbackDisputeRollupStore,
    var validator: FallbackDisputeRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDisputeRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback dispute rollup $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDisputeRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback dispute rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDisputeRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback dispute rollup $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newFallbackDisputeRollupManager(store: FallbackDisputeRollupStore, clock: Clock, flags: Registry): FallbackDisputeRollupManager {
    return FallbackDisputeRollupManager(
        store = store,
        validator = defaultFallbackDisputeRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDisputeRollupEngine(flags),
    )
}
