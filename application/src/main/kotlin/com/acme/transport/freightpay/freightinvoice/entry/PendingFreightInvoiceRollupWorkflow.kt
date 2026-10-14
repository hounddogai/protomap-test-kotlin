package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves pending freight invoice rollups through their life cycle and keeps a journal of what it did.
 */
data class PendingFreightInvoiceRollupWorkflow(
    var store: PendingFreightInvoiceRollupStore,
    var validator: PendingFreightInvoiceRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending freight invoice rollup $code not found")
        validator.requireValid(item)
        item.moveTo(PendingFreightInvoiceRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending freight invoice rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingFreightInvoiceRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newPendingFreightInvoiceRollupWorkflow(store: PendingFreightInvoiceRollupStore, clock: Clock): PendingFreightInvoiceRollupWorkflow {
    return PendingFreightInvoiceRollupWorkflow(
        store = store,
        validator = defaultPendingFreightInvoiceRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
