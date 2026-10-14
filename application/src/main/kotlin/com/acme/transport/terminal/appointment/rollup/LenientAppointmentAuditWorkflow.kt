package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.terminal.clock.Clock

/** Moves lenient appointment audits through their life cycle and keeps a journal of what it did. */
data class LenientAppointmentAuditWorkflow(
    var store: LenientAppointmentAuditStore,
    var validator: LenientAppointmentAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient appointment audit $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAppointmentAuditStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient appointment audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAppointmentAuditStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientAppointmentAuditWorkflow(store: LenientAppointmentAuditStore, clock: Clock): LenientAppointmentAuditWorkflow {
    return LenientAppointmentAuditWorkflow(
        store = store,
        validator = defaultLenientAppointmentAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
