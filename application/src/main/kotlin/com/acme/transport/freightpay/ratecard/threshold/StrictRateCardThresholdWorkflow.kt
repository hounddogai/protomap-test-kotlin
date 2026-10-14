package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.freightpay.clock.Clock

/** Moves strict rate card thresholds through their life cycle and keeps a journal of what it did. */
data class StrictRateCardThresholdCoordinator(
    var store: StrictRateCardThresholdStore,
    var validator: StrictRateCardThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictRateCardThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict rate card threshold $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRateCardThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict rate card threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRateCardThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict rate card threshold $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newStrictRateCardThresholdCoordinator(store: StrictRateCardThresholdStore, clock: Clock): StrictRateCardThresholdCoordinator {
    return StrictRateCardThresholdCoordinator(
        store = store,
        validator = defaultStrictRateCardThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictRateCardThresholdEngine(),
    )
}
