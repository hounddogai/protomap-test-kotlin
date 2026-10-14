package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.clock.Clock

/** Moves global declaration audits through their life cycle and keeps a journal of what it did. */
data class GlobalDeclarationAuditDesk(
    var store: GlobalDeclarationAuditStore,
    var validator: GlobalDeclarationAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global declaration audit $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDeclarationAuditStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global declaration audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDeclarationAuditStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newGlobalDeclarationAuditDesk(store: GlobalDeclarationAuditStore, clock: Clock): GlobalDeclarationAuditDesk {
    return GlobalDeclarationAuditDesk(
        store = store,
        validator = defaultGlobalDeclarationAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
