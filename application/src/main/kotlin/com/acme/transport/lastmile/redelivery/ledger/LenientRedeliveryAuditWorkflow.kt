package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves lenient redelivery audits through their life cycle and keeps a journal of what it did. */
data class LenientRedeliveryAuditService(
    var store: LenientRedeliveryAuditStore,
    var validator: LenientRedeliveryAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientRedeliveryAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient redelivery audit $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRedeliveryAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient redelivery audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRedeliveryAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient redelivery audit $code not found")
        val result = engine.run(item)
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

fun newLenientRedeliveryAuditService(store: LenientRedeliveryAuditStore, clock: Clock, flags: Registry): LenientRedeliveryAuditService {
    return LenientRedeliveryAuditService(
        store = store,
        validator = defaultLenientRedeliveryAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientRedeliveryAuditEngine(flags),
    )
}
