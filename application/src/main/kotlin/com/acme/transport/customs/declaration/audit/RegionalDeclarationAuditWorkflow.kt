package com.acme.transport.customs.declaration.audit

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional declaration audits through their life cycle and keeps a journal of what it did. */
data class RegionalDeclarationAuditCoordinator(
    var store: RegionalDeclarationAuditStore,
    var validator: RegionalDeclarationAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDeclarationAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional declaration audit $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDeclarationAuditStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional declaration audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDeclarationAuditStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional declaration audit $code not found")
        val result = engine.evaluate(item)
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

fun newRegionalDeclarationAuditCoordinator(store: RegionalDeclarationAuditStore, clock: Clock, flags: Registry): RegionalDeclarationAuditCoordinator {
    return RegionalDeclarationAuditCoordinator(
        store = store,
        validator = defaultRegionalDeclarationAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDeclarationAuditEngine(flags),
    )
}
