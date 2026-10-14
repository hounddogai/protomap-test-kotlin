package com.acme.transport.crewing.qualification.metric

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves scheduled qualification notices through their life cycle and keeps a journal of what it did.
 */
data class ScheduledQualificationNoticeWorkflow(
    var store: ScheduledQualificationNoticeStore,
    var validator: ScheduledQualificationNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledQualificationNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled qualification notice $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledQualificationNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled qualification notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledQualificationNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled qualification notice $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newScheduledQualificationNoticeWorkflow(store: ScheduledQualificationNoticeStore, clock: Clock, flags: Registry): ScheduledQualificationNoticeWorkflow {
    return ScheduledQualificationNoticeWorkflow(
        store = store,
        validator = defaultScheduledQualificationNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledQualificationNoticeEngine(flags),
    )
}
