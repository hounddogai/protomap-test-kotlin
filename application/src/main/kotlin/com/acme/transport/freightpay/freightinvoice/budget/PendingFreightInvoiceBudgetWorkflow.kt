package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves pending freight invoice budgets through their life cycle and keeps a journal of what it did.
 */
data class PendingFreightInvoiceBudgetWorkflow(
    var store: PendingFreightInvoiceBudgetStore,
    var validator: PendingFreightInvoiceBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending freight invoice budget $code not found")
        validator.requireValid(item)
        item.moveTo(PendingFreightInvoiceBudgetStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending freight invoice budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingFreightInvoiceBudgetStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newPendingFreightInvoiceBudgetWorkflow(store: PendingFreightInvoiceBudgetStore, clock: Clock): PendingFreightInvoiceBudgetWorkflow {
    return PendingFreightInvoiceBudgetWorkflow(
        store = store,
        validator = defaultPendingFreightInvoiceBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
