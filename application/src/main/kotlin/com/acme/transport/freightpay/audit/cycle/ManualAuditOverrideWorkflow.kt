package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.clock.Clock

/** Moves manual audit overrides through their life cycle and keeps a journal of what it did. */
data class ManualAuditOverrideService(
    var store: ManualAuditOverrideStore,
    var validator: ManualAuditOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual audit override $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAuditOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual audit override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAuditOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newManualAuditOverrideService(store: ManualAuditOverrideStore, clock: Clock): ManualAuditOverrideService {
    return ManualAuditOverrideService(
        store = store,
        validator = defaultManualAuditOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
