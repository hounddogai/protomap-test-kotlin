package com.acme.transport.terminal.appointment.metric

import com.acme.transport.terminal.clock.Clock

/** Moves pending appointment audits through their life cycle and keeps a journal of what it did. */
data class PendingAppointmentAuditWorkflow(
    var store: PendingAppointmentAuditStore,
    var validator: PendingAppointmentAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingAppointmentAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending appointment audit $code not found")
        validator.requireValid(item)
        item.moveTo(PendingAppointmentAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending appointment audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingAppointmentAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending appointment audit $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingAppointmentAuditWorkflow(store: PendingAppointmentAuditStore, clock: Clock): PendingAppointmentAuditWorkflow {
    return PendingAppointmentAuditWorkflow(
        store = store,
        validator = defaultPendingAppointmentAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingAppointmentAuditEngine(),
    )
}
