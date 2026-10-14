package com.acme.transport.network.cutoff.audit

import com.acme.transport.network.clock.Clock

/** Moves default cutoff audits through their life cycle and keeps a journal of what it did. */
data class DefaultCutoffAuditService(
    var store: DefaultCutoffAuditStore,
    var validator: DefaultCutoffAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default cutoff audit $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCutoffAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default cutoff audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCutoffAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultCutoffAuditService(store: DefaultCutoffAuditStore, clock: Clock): DefaultCutoffAuditService {
    return DefaultCutoffAuditService(
        store = store,
        validator = defaultDefaultCutoffAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
