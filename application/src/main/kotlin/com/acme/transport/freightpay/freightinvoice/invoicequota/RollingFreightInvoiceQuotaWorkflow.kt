package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves rolling freight invoice quotas through their life cycle and keeps a journal of what it did.
 */
data class RollingFreightInvoiceQuotaWorkflow(
    var store: RollingFreightInvoiceQuotaStore,
    var validator: RollingFreightInvoiceQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingFreightInvoiceQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling freight invoice quota $code not found")
        validator.requireValid(item)
        item.moveTo(RollingFreightInvoiceQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling freight invoice quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingFreightInvoiceQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling freight invoice quota $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newRollingFreightInvoiceQuotaWorkflow(store: RollingFreightInvoiceQuotaStore, clock: Clock): RollingFreightInvoiceQuotaWorkflow {
    return RollingFreightInvoiceQuotaWorkflow(
        store = store,
        validator = defaultRollingFreightInvoiceQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingFreightInvoiceQuotaEngine(),
    )
}
