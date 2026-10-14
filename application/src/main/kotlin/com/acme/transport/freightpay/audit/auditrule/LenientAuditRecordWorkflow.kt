package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.freightpay.clock.Clock

/** Moves lenient audit records through their life cycle and keeps a journal of what it did. */
data class LenientAuditRecordWorkflow(
    var store: LenientAuditRecordStore,
    var validator: LenientAuditRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient audit record $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAuditRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient audit record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAuditRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newLenientAuditRecordWorkflow(store: LenientAuditRecordStore, clock: Clock): LenientAuditRecordWorkflow {
    return LenientAuditRecordWorkflow(
        store = store,
        validator = defaultLenientAuditRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
