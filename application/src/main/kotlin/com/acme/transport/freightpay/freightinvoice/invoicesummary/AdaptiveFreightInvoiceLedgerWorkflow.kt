package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves adaptive freight invoice ledgers through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveFreightInvoiceLedgerService(
    var store: AdaptiveFreightInvoiceLedgerStore,
    var validator: AdaptiveFreightInvoiceLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveFreightInvoiceLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive freight invoice ledger $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveFreightInvoiceLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive freight invoice ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveFreightInvoiceLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive freight invoice ledger $code not found")
        val result = engine.run(item)
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

fun newAdaptiveFreightInvoiceLedgerService(store: AdaptiveFreightInvoiceLedgerStore, clock: Clock, flags: Registry): AdaptiveFreightInvoiceLedgerService {
    return AdaptiveFreightInvoiceLedgerService(
        store = store,
        validator = defaultAdaptiveFreightInvoiceLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveFreightInvoiceLedgerEngine(flags),
    )
}
