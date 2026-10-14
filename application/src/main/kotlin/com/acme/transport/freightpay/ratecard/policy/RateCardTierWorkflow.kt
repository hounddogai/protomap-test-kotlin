package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rate card tiers through their life cycle and keeps a journal of what it did. */
data class RateCardTierWorkflow(
    var store: RateCardTierStore,
    var validator: RateCardTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rate card tier $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardTierStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardTierStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rate card tier $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newRateCardTierWorkflow(store: RateCardTierStore, clock: Clock, flags: Registry): RateCardTierWorkflow {
    return RateCardTierWorkflow(
        store = store,
        validator = defaultRateCardTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardTierEngine(flags),
    )
}
