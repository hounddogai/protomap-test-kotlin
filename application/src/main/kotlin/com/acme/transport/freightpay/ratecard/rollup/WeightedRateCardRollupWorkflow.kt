package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.freightpay.clock.Clock

/** Moves weighted rate card rollups through their life cycle and keeps a journal of what it did. */
data class WeightedRateCardRollupManager(
    var store: WeightedRateCardRollupStore,
    var validator: WeightedRateCardRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedRateCardRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted rate card rollup $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRateCardRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted rate card rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRateCardRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted rate card rollup $code not found")
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

fun newWeightedRateCardRollupManager(store: WeightedRateCardRollupStore, clock: Clock): WeightedRateCardRollupManager {
    return WeightedRateCardRollupManager(
        store = store,
        validator = defaultWeightedRateCardRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedRateCardRollupEngine(),
    )
}
