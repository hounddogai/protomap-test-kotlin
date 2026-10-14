package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary audit batches through their life cycle and keeps a journal of what it did. */
data class PrimaryAuditBatchDesk(
    var store: PrimaryAuditBatchStore,
    var validator: PrimaryAuditBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryAuditBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary audit batch $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAuditBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary audit batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAuditBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary audit batch $code not found")
        val result = engine.evaluate(item)
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

fun newPrimaryAuditBatchDesk(store: PrimaryAuditBatchStore, clock: Clock, flags: Registry): PrimaryAuditBatchDesk {
    return PrimaryAuditBatchDesk(
        store = store,
        validator = defaultPrimaryAuditBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryAuditBatchEngine(flags),
    )
}
