package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves freight invoice policies through their life cycle and keeps a journal of what it did. */
data class FreightInvoicePolicyWorkflow(
    var store: FreightInvoicePolicyStore,
    var validator: FreightInvoicePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoicePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("freight invoice policy $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoicePolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoicePolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("freight invoice policy $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newFreightInvoicePolicyWorkflow(store: FreightInvoicePolicyStore, clock: Clock, flags: Registry): FreightInvoicePolicyWorkflow {
    return FreightInvoicePolicyWorkflow(
        store = store,
        validator = defaultFreightInvoicePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoicePolicyEngine(flags),
    )
}
