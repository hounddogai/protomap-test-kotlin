package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card targets through their life cycle and keeps a journal of what it did. */
data class RateCardTargetWorkflow(
    var store: RateCardTargetStore,
    var validator: RateCardTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rate card target $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newRateCardTargetWorkflow(store: RateCardTargetStore, clock: Clock): RateCardTargetWorkflow {
    return RateCardTargetWorkflow(
        store = store,
        validator = defaultRateCardTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
