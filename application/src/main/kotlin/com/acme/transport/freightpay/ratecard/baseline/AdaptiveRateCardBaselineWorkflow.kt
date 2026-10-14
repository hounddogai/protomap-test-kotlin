package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.freightpay.clock.Clock

/** Moves adaptive rate card baselines through their life cycle and keeps a journal of what it did. */
data class AdaptiveRateCardBaselineManager(
    var store: AdaptiveRateCardBaselineStore,
    var validator: AdaptiveRateCardBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive rate card baseline $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveRateCardBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive rate card baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveRateCardBaselineStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newAdaptiveRateCardBaselineManager(store: AdaptiveRateCardBaselineStore, clock: Clock): AdaptiveRateCardBaselineManager {
    return AdaptiveRateCardBaselineManager(
        store = store,
        validator = defaultAdaptiveRateCardBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
