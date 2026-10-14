package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves scheduled freight invoice snapshots through their life cycle and keeps a journal of what it did.
 */
data class ScheduledFreightInvoiceSnapshotManager(
    var store: ScheduledFreightInvoiceSnapshotStore,
    var validator: ScheduledFreightInvoiceSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled freight invoice snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledFreightInvoiceSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled freight invoice snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledFreightInvoiceSnapshotStatus.CLOSED)
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

fun newScheduledFreightInvoiceSnapshotManager(store: ScheduledFreightInvoiceSnapshotStore, clock: Clock): ScheduledFreightInvoiceSnapshotManager {
    return ScheduledFreightInvoiceSnapshotManager(
        store = store,
        validator = defaultScheduledFreightInvoiceSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
