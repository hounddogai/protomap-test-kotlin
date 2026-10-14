package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.freightpay.clock.Clock

/** Moves default rate card audits through their life cycle and keeps a journal of what it did. */
data class DefaultRateCardAuditWorkflow(
    var store: DefaultRateCardAuditStore,
    var validator: DefaultRateCardAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultRateCardAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default rate card audit $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRateCardAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default rate card audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRateCardAuditStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default rate card audit $code not found")
        val result = engine.price(item)
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

fun newDefaultRateCardAuditWorkflow(store: DefaultRateCardAuditStore, clock: Clock): DefaultRateCardAuditWorkflow {
    return DefaultRateCardAuditWorkflow(
        store = store,
        validator = defaultDefaultRateCardAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultRateCardAuditEngine(),
    )
}
