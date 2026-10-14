package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.freightpay.clock.Clock

/** Moves default audit schedules through their life cycle and keeps a journal of what it did. */
data class DefaultAuditScheduleWorkflow(
    var store: DefaultAuditScheduleStore,
    var validator: DefaultAuditScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultAuditScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default audit schedule $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAuditScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default audit schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAuditScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default audit schedule $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newDefaultAuditScheduleWorkflow(store: DefaultAuditScheduleStore, clock: Clock): DefaultAuditScheduleWorkflow {
    return DefaultAuditScheduleWorkflow(
        store = store,
        validator = defaultDefaultAuditScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultAuditScheduleEngine(),
    )
}
