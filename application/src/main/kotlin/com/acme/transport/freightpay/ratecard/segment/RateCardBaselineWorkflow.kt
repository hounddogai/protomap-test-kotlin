package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card baselines through their life cycle and keeps a journal of what it did. */
data class RateCardBaselineWorkflow(
    var store: RateCardBaselineStore,
    var validator: RateCardBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rate card baseline $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rate card baseline $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newRateCardBaselineWorkflow(store: RateCardBaselineStore, clock: Clock): RateCardBaselineWorkflow {
    return RateCardBaselineWorkflow(
        store = store,
        validator = defaultRateCardBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardBaselineEngine(),
    )
}
