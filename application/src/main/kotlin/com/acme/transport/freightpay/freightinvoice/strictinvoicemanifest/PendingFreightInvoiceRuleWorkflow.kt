package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.freightpay.clock.Clock

/** Moves pending freight invoice rules through their life cycle and keeps a journal of what it did. */
data class PendingFreightInvoiceRuleCoordinator(
    var store: PendingFreightInvoiceRuleStore,
    var validator: PendingFreightInvoiceRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingFreightInvoiceRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending freight invoice rule $code not found")
        validator.requireValid(item)
        item.moveTo(PendingFreightInvoiceRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending freight invoice rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingFreightInvoiceRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending freight invoice rule $code not found")
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

fun newPendingFreightInvoiceRuleCoordinator(store: PendingFreightInvoiceRuleStore, clock: Clock): PendingFreightInvoiceRuleCoordinator {
    return PendingFreightInvoiceRuleCoordinator(
        store = store,
        validator = defaultPendingFreightInvoiceRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingFreightInvoiceRuleEngine(),
    )
}
