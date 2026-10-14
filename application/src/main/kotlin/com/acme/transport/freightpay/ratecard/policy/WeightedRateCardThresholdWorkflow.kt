package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves weighted rate card thresholds through their life cycle and keeps a journal of what it did. */
data class WeightedRateCardThresholdCoordinator(
    var store: WeightedRateCardThresholdStore,
    var validator: WeightedRateCardThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedRateCardThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted rate card threshold $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRateCardThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted rate card threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRateCardThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted rate card threshold $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newWeightedRateCardThresholdCoordinator(store: WeightedRateCardThresholdStore, clock: Clock, flags: Registry): WeightedRateCardThresholdCoordinator {
    return WeightedRateCardThresholdCoordinator(
        store = store,
        validator = defaultWeightedRateCardThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedRateCardThresholdEngine(flags),
    )
}
