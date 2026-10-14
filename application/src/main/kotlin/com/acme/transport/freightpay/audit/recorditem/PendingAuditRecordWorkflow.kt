package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.freightpay.clock.Clock

/** Moves pending audit records through their life cycle and keeps a journal of what it did. */
data class PendingAuditRecordManager(
    var store: PendingAuditRecordStore,
    var validator: PendingAuditRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending audit record $code not found")
        validator.requireValid(item)
        item.moveTo(PendingAuditRecordStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending audit record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingAuditRecordStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newPendingAuditRecordManager(store: PendingAuditRecordStore, clock: Clock): PendingAuditRecordManager {
    return PendingAuditRecordManager(
        store = store,
        validator = defaultPendingAuditRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
