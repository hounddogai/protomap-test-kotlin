package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves tiered freight invoice variances through their life cycle and keeps a journal of what it did.
 */
data class TieredFreightInvoiceVarianceCoordinator(
    var store: TieredFreightInvoiceVarianceStore,
    var validator: TieredFreightInvoiceVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered freight invoice variance $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFreightInvoiceVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered freight invoice variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFreightInvoiceVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredFreightInvoiceVarianceCoordinator(store: TieredFreightInvoiceVarianceStore, clock: Clock): TieredFreightInvoiceVarianceCoordinator {
    return TieredFreightInvoiceVarianceCoordinator(
        store = store,
        validator = defaultTieredFreightInvoiceVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
