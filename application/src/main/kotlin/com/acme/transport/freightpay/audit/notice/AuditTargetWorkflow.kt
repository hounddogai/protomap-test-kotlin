package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves audit targets through their life cycle and keeps a journal of what it did. */
data class AuditTargetCoordinator(
    var store: AuditTargetStore,
    var validator: AuditTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("audit target $code not found")
        validator.requireValid(item)
        item.moveTo(AuditTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("audit target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("audit target $code not found")
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

fun newAuditTargetCoordinator(store: AuditTargetStore, clock: Clock, flags: Registry): AuditTargetCoordinator {
    return AuditTargetCoordinator(
        store = store,
        validator = defaultAuditTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditTargetEngine(flags),
    )
}
