package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves manual fuel surcharge variances through their life cycle and keeps a journal of what it did.
 */
data class ManualFuelSurchargeVarianceManager(
    var store: ManualFuelSurchargeVarianceStore,
    var validator: ManualFuelSurchargeVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual fuel surcharge variance $code not found")
        validator.requireValid(item)
        item.moveTo(ManualFuelSurchargeVarianceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual fuel surcharge variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualFuelSurchargeVarianceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newManualFuelSurchargeVarianceManager(store: ManualFuelSurchargeVarianceStore, clock: Clock): ManualFuelSurchargeVarianceManager {
    return ManualFuelSurchargeVarianceManager(
        store = store,
        validator = defaultManualFuelSurchargeVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
