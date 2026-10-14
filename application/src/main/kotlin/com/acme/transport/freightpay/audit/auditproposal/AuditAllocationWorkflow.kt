package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.clock.Clock

/** Moves audit allocations through their life cycle and keeps a journal of what it did. */
data class AuditAllocationDesk(
    var store: AuditAllocationStore,
    var validator: AuditAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("audit allocation $code not found")
        validator.requireValid(item)
        item.moveTo(AuditAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("audit allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAuditAllocationDesk(store: AuditAllocationStore, clock: Clock): AuditAllocationDesk {
    return AuditAllocationDesk(
        store = store,
        validator = defaultAuditAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
