package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default rate card cycles through their life cycle and keeps a journal of what it did. */
data class DefaultRateCardCycleWorkflow(
    var store: DefaultRateCardCycleStore,
    var validator: DefaultRateCardCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultRateCardCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default rate card cycle $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRateCardCycleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default rate card cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRateCardCycleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default rate card cycle $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newDefaultRateCardCycleWorkflow(store: DefaultRateCardCycleStore, clock: Clock, flags: Registry): DefaultRateCardCycleWorkflow {
    return DefaultRateCardCycleWorkflow(
        store = store,
        validator = defaultDefaultRateCardCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultRateCardCycleEngine(flags),
    )
}
