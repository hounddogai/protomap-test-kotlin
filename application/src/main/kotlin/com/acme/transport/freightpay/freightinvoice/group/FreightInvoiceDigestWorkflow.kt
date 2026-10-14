package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves freight invoice digests through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceDigestWorkflow(
    var store: FreightInvoiceDigestStore,
    var validator: FreightInvoiceDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("freight invoice digest $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceDigestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("freight invoice digest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newFreightInvoiceDigestWorkflow(store: FreightInvoiceDigestStore, clock: Clock, flags: Registry): FreightInvoiceDigestWorkflow {
    return FreightInvoiceDigestWorkflow(
        store = store,
        validator = defaultFreightInvoiceDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceDigestEngine(flags),
    )
}
