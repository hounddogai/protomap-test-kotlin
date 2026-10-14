package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card profiles through their life cycle and keeps a journal of what it did. */
data class RateCardProfileWorkflow(
    var store: RateCardProfileStore,
    var validator: RateCardProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rate card profile $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardProfileStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardProfileStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newRateCardProfileWorkflow(store: RateCardProfileStore, clock: Clock): RateCardProfileWorkflow {
    return RateCardProfileWorkflow(
        store = store,
        validator = defaultRateCardProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
