package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.clock.Clock

/** Moves strict rate card plans through their life cycle and keeps a journal of what it did. */
data class StrictRateCardPlanManager(
    var store: StrictRateCardPlanStore,
    var validator: StrictRateCardPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict rate card plan $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRateCardPlanStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict rate card plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRateCardPlanStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newStrictRateCardPlanManager(store: StrictRateCardPlanStore, clock: Clock): StrictRateCardPlanManager {
    return StrictRateCardPlanManager(
        store = store,
        validator = defaultStrictRateCardPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
