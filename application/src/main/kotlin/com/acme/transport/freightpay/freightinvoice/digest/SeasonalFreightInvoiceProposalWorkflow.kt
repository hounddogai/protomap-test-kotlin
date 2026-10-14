package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves seasonal freight invoice proposals through their life cycle and keeps a journal of what it did.
 */
data class SeasonalFreightInvoiceProposalService(
    var store: SeasonalFreightInvoiceProposalStore,
    var validator: SeasonalFreightInvoiceProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalFreightInvoiceProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal freight invoice proposal $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalFreightInvoiceProposalStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal freight invoice proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalFreightInvoiceProposalStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("seasonal freight invoice proposal $code not found")
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

fun newSeasonalFreightInvoiceProposalService(store: SeasonalFreightInvoiceProposalStore, clock: Clock, flags: Registry): SeasonalFreightInvoiceProposalService {
    return SeasonalFreightInvoiceProposalService(
        store = store,
        validator = defaultSeasonalFreightInvoiceProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalFreightInvoiceProposalEngine(flags),
    )
}
