package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge assignments through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeAssignmentWorkflow(
    var store: FuelSurchargeAssignmentStore,
    var validator: FuelSurchargeAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel surcharge assignment $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeAssignmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeAssignmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newFuelSurchargeAssignmentWorkflow(store: FuelSurchargeAssignmentStore, clock: Clock): FuelSurchargeAssignmentWorkflow {
    return FuelSurchargeAssignmentWorkflow(
        store = store,
        validator = defaultFuelSurchargeAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
