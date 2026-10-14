package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves fallback node variances through their life cycle and keeps a journal of what it did. */
data class FallbackNodeVarianceManager(
    var store: FallbackNodeVarianceStore,
    var validator: FallbackNodeVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackNodeVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback node variance $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackNodeVarianceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback node variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackNodeVarianceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback node variance $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newFallbackNodeVarianceManager(store: FallbackNodeVarianceStore, clock: Clock, flags: Registry): FallbackNodeVarianceManager {
    return FallbackNodeVarianceManager(
        store = store,
        validator = defaultFallbackNodeVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackNodeVarianceEngine(flags),
    )
}
