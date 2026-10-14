package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves regional fuel surcharge audits through their life cycle and keeps a journal of what it did.
 */
data class RegionalFuelSurchargeAuditManager(
    var store: RegionalFuelSurchargeAuditStore,
    var validator: RegionalFuelSurchargeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional fuel surcharge audit $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalFuelSurchargeAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional fuel surcharge audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalFuelSurchargeAuditStatus.CLOSED)
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

fun newRegionalFuelSurchargeAuditManager(store: RegionalFuelSurchargeAuditStore, clock: Clock): RegionalFuelSurchargeAuditManager {
    return RegionalFuelSurchargeAuditManager(
        store = store,
        validator = defaultRegionalFuelSurchargeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
