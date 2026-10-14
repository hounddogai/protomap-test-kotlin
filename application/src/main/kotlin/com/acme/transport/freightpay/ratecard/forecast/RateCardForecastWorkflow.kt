package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card forecasts through their life cycle and keeps a journal of what it did. */
data class RateCardForecastWorkflow(
    var store: RateCardForecastStore,
    var validator: RateCardForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rate card forecast $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rate card forecast $code not found")
        val result = engine.price(item)
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

fun newRateCardForecastWorkflow(store: RateCardForecastStore, clock: Clock): RateCardForecastWorkflow {
    return RateCardForecastWorkflow(
        store = store,
        validator = defaultRateCardForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardForecastEngine(),
    )
}
