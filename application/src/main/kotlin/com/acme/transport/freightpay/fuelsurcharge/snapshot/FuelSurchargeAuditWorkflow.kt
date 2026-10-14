package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge audits through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeAuditManager(
    var store: FuelSurchargeAuditStore,
    var validator: FuelSurchargeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel surcharge audit $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newFuelSurchargeAuditManager(store: FuelSurchargeAuditStore, clock: Clock): FuelSurchargeAuditManager {
    return FuelSurchargeAuditManager(
        store = store,
        validator = defaultFuelSurchargeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
