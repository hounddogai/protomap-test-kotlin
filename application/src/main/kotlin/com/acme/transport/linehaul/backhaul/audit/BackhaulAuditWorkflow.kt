package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul audits through their life cycle and keeps a journal of what it did. */
data class BackhaulAuditService(
    var store: BackhaulAuditStore,
    var validator: BackhaulAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("backhaul audit $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulAuditStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulAuditStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newBackhaulAuditService(store: BackhaulAuditStore, clock: Clock): BackhaulAuditService {
    return BackhaulAuditService(
        store = store,
        validator = defaultBackhaulAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
