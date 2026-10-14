package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card variances through their life cycle and keeps a journal of what it did. */
data class RateCardVarianceWorkflow(
    var store: RateCardVarianceStore,
    var validator: RateCardVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rate card variance $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newRateCardVarianceWorkflow(store: RateCardVarianceStore, clock: Clock): RateCardVarianceWorkflow {
    return RateCardVarianceWorkflow(
        store = store,
        validator = defaultRateCardVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
