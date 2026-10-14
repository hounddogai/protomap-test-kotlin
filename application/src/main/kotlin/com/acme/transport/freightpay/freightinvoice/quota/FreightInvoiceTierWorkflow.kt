package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves freight invoice tiers through their life cycle and keeps a journal of what it did. */
data class FreightInvoiceTierManager(
    var store: FreightInvoiceTierStore,
    var validator: FreightInvoiceTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FreightInvoiceTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("freight invoice tier $code not found")
        validator.requireValid(item)
        item.moveTo(FreightInvoiceTierStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("freight invoice tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FreightInvoiceTierStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("freight invoice tier $code not found")
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

fun newFreightInvoiceTierManager(store: FreightInvoiceTierStore, clock: Clock, flags: Registry): FreightInvoiceTierManager {
    return FreightInvoiceTierManager(
        store = store,
        validator = defaultFreightInvoiceTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFreightInvoiceTierEngine(flags),
    )
}
