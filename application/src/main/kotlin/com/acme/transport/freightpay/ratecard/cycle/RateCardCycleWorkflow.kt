package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rate card cycles through their life cycle and keeps a journal of what it did. */
data class RateCardCycleManager(
    var store: RateCardCycleStore,
    var validator: RateCardCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rate card cycle $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardCycleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardCycleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rate card cycle $code not found")
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

fun newRateCardCycleManager(store: RateCardCycleStore, clock: Clock, flags: Registry): RateCardCycleManager {
    return RateCardCycleManager(
        store = store,
        validator = defaultRateCardCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardCycleEngine(flags),
    )
}
