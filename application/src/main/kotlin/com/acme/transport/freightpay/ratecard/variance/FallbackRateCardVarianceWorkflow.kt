package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback rate card variances through their life cycle and keeps a journal of what it did. */
data class FallbackRateCardVarianceCoordinator(
    var store: FallbackRateCardVarianceStore,
    var validator: FallbackRateCardVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRateCardVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback rate card variance $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRateCardVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback rate card variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRateCardVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback rate card variance $code not found")
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

fun newFallbackRateCardVarianceCoordinator(store: FallbackRateCardVarianceStore, clock: Clock, flags: Registry): FallbackRateCardVarianceCoordinator {
    return FallbackRateCardVarianceCoordinator(
        store = store,
        validator = defaultFallbackRateCardVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRateCardVarianceEngine(flags),
    )
}
