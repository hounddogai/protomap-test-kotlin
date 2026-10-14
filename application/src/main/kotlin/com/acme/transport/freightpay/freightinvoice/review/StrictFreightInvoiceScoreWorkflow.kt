package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.freightpay.clock.Clock

/** Moves strict freight invoice scores through their life cycle and keeps a journal of what it did. */
data class StrictFreightInvoiceScoreCoordinator(
    var store: StrictFreightInvoiceScoreStore,
    var validator: StrictFreightInvoiceScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictFreightInvoiceScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict freight invoice score $code not found")
        validator.requireValid(item)
        item.moveTo(StrictFreightInvoiceScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict freight invoice score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictFreightInvoiceScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict freight invoice score $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newStrictFreightInvoiceScoreCoordinator(store: StrictFreightInvoiceScoreStore, clock: Clock): StrictFreightInvoiceScoreCoordinator {
    return StrictFreightInvoiceScoreCoordinator(
        store = store,
        validator = defaultStrictFreightInvoiceScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictFreightInvoiceScoreEngine(),
    )
}
