package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy audit records through their life cycle and keeps a journal of what it did. */
data class LegacyAuditRecordService(
    var store: LegacyAuditRecordStore,
    var validator: LegacyAuditRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy audit record $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAuditRecordStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy audit record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAuditRecordStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLegacyAuditRecordService(store: LegacyAuditRecordStore, clock: Clock): LegacyAuditRecordService {
    return LegacyAuditRecordService(
        store = store,
        validator = defaultLegacyAuditRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
