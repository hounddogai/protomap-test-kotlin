package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge segments through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeSegmentWorkflow(
    var store: FuelSurchargeSegmentStore,
    var validator: FuelSurchargeSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel surcharge segment $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeSegmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeSegmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newFuelSurchargeSegmentWorkflow(store: FuelSurchargeSegmentStore, clock: Clock): FuelSurchargeSegmentWorkflow {
    return FuelSurchargeSegmentWorkflow(
        store = store,
        validator = defaultFuelSurchargeSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
