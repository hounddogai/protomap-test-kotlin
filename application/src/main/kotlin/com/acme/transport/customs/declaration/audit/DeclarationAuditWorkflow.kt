package com.acme.transport.customs.declaration.audit

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration audits through their life cycle and keeps a journal of what it did. */
data class DeclarationAuditWorkflow(
    var store: DeclarationAuditStore,
    var validator: DeclarationAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration audit $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationAuditStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("declaration audit $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDeclarationAuditWorkflow(store: DeclarationAuditStore, clock: Clock, flags: Registry): DeclarationAuditWorkflow {
    return DeclarationAuditWorkflow(
        store = store,
        validator = defaultDeclarationAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationAuditEngine(flags),
    )
}
