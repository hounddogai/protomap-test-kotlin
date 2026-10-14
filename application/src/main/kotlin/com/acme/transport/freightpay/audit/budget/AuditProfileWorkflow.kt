package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves audit profiles through their life cycle and keeps a journal of what it did. */
data class AuditProfileCoordinator(
    var store: AuditProfileStore,
    var validator: AuditProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("audit profile $code not found")
        validator.requireValid(item)
        item.moveTo(AuditProfileStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("audit profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditProfileStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("audit profile $code not found")
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

fun newAuditProfileCoordinator(store: AuditProfileStore, clock: Clock, flags: Registry): AuditProfileCoordinator {
    return AuditProfileCoordinator(
        store = store,
        validator = defaultAuditProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditProfileEngine(flags),
    )
}
