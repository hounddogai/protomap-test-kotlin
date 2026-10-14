package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves pending freight invoice manifests through their life cycle and keeps a journal of what it did.
 */
data class PendingFreightInvoiceManifestDesk(
    var store: PendingFreightInvoiceManifestStore,
    var validator: PendingFreightInvoiceManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingFreightInvoiceManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending freight invoice manifest $code not found")
        validator.requireValid(item)
        item.moveTo(PendingFreightInvoiceManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending freight invoice manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingFreightInvoiceManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending freight invoice manifest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingFreightInvoiceManifestDesk(store: PendingFreightInvoiceManifestStore, clock: Clock, flags: Registry): PendingFreightInvoiceManifestDesk {
    return PendingFreightInvoiceManifestDesk(
        store = store,
        validator = defaultPendingFreightInvoiceManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingFreightInvoiceManifestEngine(flags),
    )
}
