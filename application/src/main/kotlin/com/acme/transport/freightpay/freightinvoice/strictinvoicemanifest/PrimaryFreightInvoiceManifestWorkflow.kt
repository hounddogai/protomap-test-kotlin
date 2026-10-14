package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves primary freight invoice manifests through their life cycle and keeps a journal of what it did.
 */
data class PrimaryFreightInvoiceManifestDesk(
    var store: PrimaryFreightInvoiceManifestStore,
    var validator: PrimaryFreightInvoiceManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryFreightInvoiceManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary freight invoice manifest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryFreightInvoiceManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary freight invoice manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryFreightInvoiceManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary freight invoice manifest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newPrimaryFreightInvoiceManifestDesk(store: PrimaryFreightInvoiceManifestStore, clock: Clock, flags: Registry): PrimaryFreightInvoiceManifestDesk {
    return PrimaryFreightInvoiceManifestDesk(
        store = store,
        validator = defaultPrimaryFreightInvoiceManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryFreightInvoiceManifestEngine(flags),
    )
}
