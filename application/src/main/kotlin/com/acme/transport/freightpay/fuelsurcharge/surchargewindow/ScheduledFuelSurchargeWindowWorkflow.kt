package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves scheduled fuel surcharge windows through their life cycle and keeps a journal of what it did.
 */
data class ScheduledFuelSurchargeWindowManager(
    var store: ScheduledFuelSurchargeWindowStore,
    var validator: ScheduledFuelSurchargeWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledFuelSurchargeWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled fuel surcharge window $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledFuelSurchargeWindowStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled fuel surcharge window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledFuelSurchargeWindowStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled fuel surcharge window $code not found")
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

fun newScheduledFuelSurchargeWindowManager(store: ScheduledFuelSurchargeWindowStore, clock: Clock): ScheduledFuelSurchargeWindowManager {
    return ScheduledFuelSurchargeWindowManager(
        store = store,
        validator = defaultScheduledFuelSurchargeWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledFuelSurchargeWindowEngine(),
    )
}
