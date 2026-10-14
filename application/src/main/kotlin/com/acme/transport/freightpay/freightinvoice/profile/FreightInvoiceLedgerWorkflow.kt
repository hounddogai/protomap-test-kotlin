package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.freightpay.clock.Clock

/** Moves freight invoice ledgers through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceLedgerCoordinator(
    var store: FreightInvoiceLedgerStore,
    var validator: FreightInvoiceLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("freight invoice ledger $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceLedgerStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceLedgerStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newFreightInvoiceLedgerCoordinator(store: FreightInvoiceLedgerStore, clock: Clock): FreightInvoiceLedgerCoordinator {
    return FreightInvoiceLedgerCoordinator(
        store = store,
        validator = defaultFreightInvoiceLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
