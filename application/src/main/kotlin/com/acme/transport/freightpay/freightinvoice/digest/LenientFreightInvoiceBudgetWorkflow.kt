package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves lenient freight invoice budgets through their life cycle and keeps a journal of what it did.
 */
data class LenientFreightInvoiceBudgetWorkflow(
    var store: LenientFreightInvoiceBudgetStore,
    var validator: LenientFreightInvoiceBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient freight invoice budget $code not found")
        validator.requireValid(item)
        item.moveTo(LenientFreightInvoiceBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient freight invoice budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientFreightInvoiceBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLenientFreightInvoiceBudgetWorkflow(store: LenientFreightInvoiceBudgetStore, clock: Clock): LenientFreightInvoiceBudgetWorkflow {
    return LenientFreightInvoiceBudgetWorkflow(
        store = store,
        validator = defaultLenientFreightInvoiceBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
