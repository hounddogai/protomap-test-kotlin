package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card rollups through their life cycle and keeps a journal of what it did. */
data class RateCardRollupCoordinator(
    var store: RateCardRollupStore,
    var validator: RateCardRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rate card rollup $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRateCardRollupCoordinator(store: RateCardRollupStore, clock: Clock): RateCardRollupCoordinator {
    return RateCardRollupCoordinator(
        store = store,
        validator = defaultRateCardRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
