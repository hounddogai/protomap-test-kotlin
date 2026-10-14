package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.clock.Clock

/** Moves scheduled audit bundles through their life cycle and keeps a journal of what it did. */
data class ScheduledAuditBundleManager(
    var store: ScheduledAuditBundleStore,
    var validator: ScheduledAuditBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled audit bundle $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAuditBundleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled audit bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAuditBundleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newScheduledAuditBundleManager(store: ScheduledAuditBundleStore, clock: Clock): ScheduledAuditBundleManager {
    return ScheduledAuditBundleManager(
        store = store,
        validator = defaultScheduledAuditBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
