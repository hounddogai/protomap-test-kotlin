package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves global freight invoice rollups through their life cycle and keeps a journal of what it did.
 */
data class GlobalFreightInvoiceRollupWorkflow(
    var store: GlobalFreightInvoiceRollupStore,
    var validator: GlobalFreightInvoiceRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalFreightInvoiceRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global freight invoice rollup $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalFreightInvoiceRollupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global freight invoice rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalFreightInvoiceRollupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global freight invoice rollup $code not found")
        val result = engine.evaluate(item)
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

fun newGlobalFreightInvoiceRollupWorkflow(store: GlobalFreightInvoiceRollupStore, clock: Clock, flags: Registry): GlobalFreightInvoiceRollupWorkflow {
    return GlobalFreightInvoiceRollupWorkflow(
        store = store,
        validator = defaultGlobalFreightInvoiceRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalFreightInvoiceRollupEngine(flags),
    )
}
