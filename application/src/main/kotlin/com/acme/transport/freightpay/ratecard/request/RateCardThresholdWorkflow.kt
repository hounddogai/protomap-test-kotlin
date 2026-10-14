package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rate card thresholds through their life cycle and keeps a journal of what it did. */
data class RateCardThresholdService(
    var store: RateCardThresholdStore,
    var validator: RateCardThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rate card threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rate card threshold $code not found")
        val result = engine.run(item)
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

fun newRateCardThresholdService(store: RateCardThresholdStore, clock: Clock, flags: Registry): RateCardThresholdService {
    return RateCardThresholdService(
        store = store,
        validator = defaultRateCardThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardThresholdEngine(flags),
    )
}
