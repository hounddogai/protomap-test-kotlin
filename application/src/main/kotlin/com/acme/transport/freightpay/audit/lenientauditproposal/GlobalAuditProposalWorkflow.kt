package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global audit proposals through their life cycle and keeps a journal of what it did. */
data class GlobalAuditProposalDesk(
    var store: GlobalAuditProposalStore,
    var validator: GlobalAuditProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalAuditProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global audit proposal $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalAuditProposalStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global audit proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalAuditProposalStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global audit proposal $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newGlobalAuditProposalDesk(store: GlobalAuditProposalStore, clock: Clock, flags: Registry): GlobalAuditProposalDesk {
    return GlobalAuditProposalDesk(
        store = store,
        validator = defaultGlobalAuditProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalAuditProposalEngine(flags),
    )
}
