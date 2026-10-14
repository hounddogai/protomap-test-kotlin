package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rate card estimates through their life cycle and keeps a journal of what it did. */
data class RateCardEstimateService(
    var store: RateCardEstimateStore,
    var validator: RateCardEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rate card estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rate card estimate $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRateCardEstimateService(store: RateCardEstimateStore, clock: Clock, flags: Registry): RateCardEstimateService {
    return RateCardEstimateService(
        store = store,
        validator = defaultRateCardEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardEstimateEngine(flags),
    )
}
