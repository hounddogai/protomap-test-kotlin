package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.clock.Clock

/** Moves audit drafts through their life cycle and keeps a journal of what it did. */
data class AuditDraftWorkflow(
    var store: AuditDraftStore,
    var validator: AuditDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("audit draft $code not found")
        validator.requireValid(item)
        item.moveTo(AuditDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("audit draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditDraftStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newAuditDraftWorkflow(store: AuditDraftStore, clock: Clock): AuditDraftWorkflow {
    return AuditDraftWorkflow(
        store = store,
        validator = defaultAuditDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
