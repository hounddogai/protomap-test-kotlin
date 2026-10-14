package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves swap variances through their life cycle and keeps a journal of what it did. */
data class SwapVarianceCoordinator(
    var store: SwapVarianceStore,
    var validator: SwapVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("swap variance $code not found")
        validator.requireValid(item)
        item.moveTo(SwapVarianceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("swap variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapVarianceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("swap variance $code not found")
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

fun newSwapVarianceCoordinator(store: SwapVarianceStore, clock: Clock, flags: Registry): SwapVarianceCoordinator {
    return SwapVarianceCoordinator(
        store = store,
        validator = defaultSwapVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapVarianceEngine(flags),
    )
}
