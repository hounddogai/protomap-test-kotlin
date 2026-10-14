package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge cycles through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeCycleService(
    var store: FuelSurchargeCycleStore,
    var validator: FuelSurchargeCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel surcharge cycle $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeCycleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeCycleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge cycle $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newFuelSurchargeCycleService(store: FuelSurchargeCycleStore, clock: Clock): FuelSurchargeCycleService {
    return FuelSurchargeCycleService(
        store = store,
        validator = defaultFuelSurchargeCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeCycleEngine(),
    )
}
