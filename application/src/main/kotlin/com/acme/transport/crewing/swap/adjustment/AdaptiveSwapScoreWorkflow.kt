package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.clock.Clock

/** Moves adaptive swap scores through their life cycle and keeps a journal of what it did. */
data class AdaptiveSwapScoreDesk(
    var store: AdaptiveSwapScoreStore,
    var validator: AdaptiveSwapScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive swap score $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveSwapScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive swap score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveSwapScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newAdaptiveSwapScoreDesk(store: AdaptiveSwapScoreStore, clock: Clock): AdaptiveSwapScoreDesk {
    return AdaptiveSwapScoreDesk(
        store = store,
        validator = defaultAdaptiveSwapScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
