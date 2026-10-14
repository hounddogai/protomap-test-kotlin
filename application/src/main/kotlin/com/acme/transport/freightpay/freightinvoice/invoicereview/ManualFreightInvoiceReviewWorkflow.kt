package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves manual freight invoice reviews through their life cycle and keeps a journal of what it did.
 */
data class ManualFreightInvoiceReviewWorkflow(
    var store: ManualFreightInvoiceReviewStore,
    var validator: ManualFreightInvoiceReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual freight invoice review $code not found")
        validator.requireValid(item)
        item.moveTo(ManualFreightInvoiceReviewStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual freight invoice review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualFreightInvoiceReviewStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newManualFreightInvoiceReviewWorkflow(store: ManualFreightInvoiceReviewStore, clock: Clock): ManualFreightInvoiceReviewWorkflow {
    return ManualFreightInvoiceReviewWorkflow(
        store = store,
        validator = defaultManualFreightInvoiceReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
