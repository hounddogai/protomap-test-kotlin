package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge reviews through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeReviewService(
    var store: FuelSurchargeReviewStore,
    var validator: FuelSurchargeReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel surcharge review $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeReviewStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeReviewStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge review $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newFuelSurchargeReviewService(store: FuelSurchargeReviewStore, clock: Clock): FuelSurchargeReviewService {
    return FuelSurchargeReviewService(
        store = store,
        validator = defaultFuelSurchargeReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeReviewEngine(),
    )
}
