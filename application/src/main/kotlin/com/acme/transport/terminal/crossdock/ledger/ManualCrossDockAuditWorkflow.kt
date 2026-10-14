package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.terminal.clock.Clock

/** Moves manual cross dock audits through their life cycle and keeps a journal of what it did. */
data class ManualCrossDockAuditManager(
    var store: ManualCrossDockAuditStore,
    var validator: ManualCrossDockAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual cross dock audit $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCrossDockAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual cross dock audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCrossDockAuditStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newManualCrossDockAuditManager(store: ManualCrossDockAuditStore, clock: Clock): ManualCrossDockAuditManager {
    return ManualCrossDockAuditManager(
        store = store,
        validator = defaultManualCrossDockAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
