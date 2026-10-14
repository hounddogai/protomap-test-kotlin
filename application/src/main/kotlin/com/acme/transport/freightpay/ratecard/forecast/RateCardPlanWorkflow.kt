package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card plans through their life cycle and keeps a journal of what it did. */
data class RateCardPlanService(
    var store: RateCardPlanStore,
    var validator: RateCardPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rate card plan $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardPlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRateCardPlanService(store: RateCardPlanStore, clock: Clock): RateCardPlanService {
    return RateCardPlanService(
        store = store,
        validator = defaultRateCardPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
