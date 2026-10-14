package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive rate card plans through their life cycle and keeps a journal of what it did. */
data class AdaptiveRateCardPlanManager(
    var store: AdaptiveRateCardPlanStore,
    var validator: AdaptiveRateCardPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveRateCardPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive rate card plan $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveRateCardPlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive rate card plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveRateCardPlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive rate card plan $code not found")
        val result = engine.evaluate(item)
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

fun newAdaptiveRateCardPlanManager(store: AdaptiveRateCardPlanStore, clock: Clock, flags: Registry): AdaptiveRateCardPlanManager {
    return AdaptiveRateCardPlanManager(
        store = store,
        validator = defaultAdaptiveRateCardPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveRateCardPlanEngine(flags),
    )
}
