package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient backhaul audits through their life cycle and keeps a journal of what it did. */
data class LenientBackhaulAuditService(
    var store: LenientBackhaulAuditStore,
    var validator: LenientBackhaulAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient backhaul audit $code not found")
        validator.requireValid(item)
        item.moveTo(LenientBackhaulAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient backhaul audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientBackhaulAuditStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientBackhaulAuditService(store: LenientBackhaulAuditStore, clock: Clock): LenientBackhaulAuditService {
    return LenientBackhaulAuditService(
        store = store,
        validator = defaultLenientBackhaulAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
