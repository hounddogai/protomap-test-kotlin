package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.freightpay.clock.Clock

/** Moves capped rate card rollups through their life cycle and keeps a journal of what it did. */
data class CappedRateCardRollupWorkflow(
    var store: CappedRateCardRollupStore,
    var validator: CappedRateCardRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped rate card rollup $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRateCardRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped rate card rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRateCardRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newCappedRateCardRollupWorkflow(store: CappedRateCardRollupStore, clock: Clock): CappedRateCardRollupWorkflow {
    return CappedRateCardRollupWorkflow(
        store = store,
        validator = defaultCappedRateCardRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
