package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves freight invoice snapshots through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceSnapshotDesk(
    var store: FreightInvoiceSnapshotStore,
    var validator: FreightInvoiceSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("freight invoice snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceSnapshotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceSnapshotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("freight invoice snapshot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newFreightInvoiceSnapshotDesk(store: FreightInvoiceSnapshotStore, clock: Clock, flags: Registry): FreightInvoiceSnapshotDesk {
    return FreightInvoiceSnapshotDesk(
        store = store,
        validator = defaultFreightInvoiceSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceSnapshotEngine(flags),
    )
}
