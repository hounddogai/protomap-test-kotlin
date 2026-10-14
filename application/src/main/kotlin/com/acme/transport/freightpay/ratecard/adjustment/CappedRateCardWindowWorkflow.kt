package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves capped rate card windows through their life cycle and keeps a journal of what it did. */
data class CappedRateCardWindowWorkflow(
    var store: CappedRateCardWindowStore,
    var validator: CappedRateCardWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped rate card window $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRateCardWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped rate card window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRateCardWindowStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCappedRateCardWindowWorkflow(store: CappedRateCardWindowStore, clock: Clock): CappedRateCardWindowWorkflow {
    return CappedRateCardWindowWorkflow(
        store = store,
        validator = defaultCappedRateCardWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
