package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves manual fuel surcharge schedules through their life cycle and keeps a journal of what it did.
 */
data class ManualFuelSurchargeScheduleWorkflow(
    var store: ManualFuelSurchargeScheduleStore,
    var validator: ManualFuelSurchargeScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual fuel surcharge schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ManualFuelSurchargeScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual fuel surcharge schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualFuelSurchargeScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newManualFuelSurchargeScheduleWorkflow(store: ManualFuelSurchargeScheduleStore, clock: Clock): ManualFuelSurchargeScheduleWorkflow {
    return ManualFuelSurchargeScheduleWorkflow(
        store = store,
        validator = defaultManualFuelSurchargeScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
