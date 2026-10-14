package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.clock.Clock

/** Moves clearance audits through their life cycle and keeps a journal of what it did. */
data class ClearanceAuditDesk(
    var store: ClearanceAuditStore,
    var validator: ClearanceAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("clearance audit $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceAuditStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceAuditStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("clearance audit $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newClearanceAuditDesk(store: ClearanceAuditStore, clock: Clock): ClearanceAuditDesk {
    return ClearanceAuditDesk(
        store = store,
        validator = defaultClearanceAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceAuditEngine(),
    )
}
