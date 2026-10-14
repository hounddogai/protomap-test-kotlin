package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card requests through their life cycle and keeps a journal of what it did. */
data class RateCardRequestService(
    var store: RateCardRequestStore,
    var validator: RateCardRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rate card request $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardRequestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardRequestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rate card request $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRateCardRequestService(store: RateCardRequestStore, clock: Clock): RateCardRequestService {
    return RateCardRequestService(
        store = store,
        validator = defaultRateCardRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardRequestEngine(),
    )
}
