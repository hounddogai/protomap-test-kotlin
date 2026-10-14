package com.acme.transport.freightpay.audit.exception

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves audit exceptions through their life cycle and keeps a journal of what it did. */
data class AuditExceptionManager(
    var store: AuditExceptionStore,
    var validator: AuditExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("audit exception $code not found")
        validator.requireValid(item)
        item.moveTo(AuditExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("audit exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("audit exception $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newAuditExceptionManager(store: AuditExceptionStore, clock: Clock, flags: Registry): AuditExceptionManager {
    return AuditExceptionManager(
        store = store,
        validator = defaultAuditExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditExceptionEngine(flags),
    )
}
