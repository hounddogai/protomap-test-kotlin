package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection notices through their life cycle and keeps a journal of what it did. */
data class InspectionNoticeDesk(
    var store: InspectionNoticeStore,
    var validator: InspectionNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection notice $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("inspection notice $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newInspectionNoticeDesk(store: InspectionNoticeStore, clock: Clock, flags: Registry): InspectionNoticeDesk {
    return InspectionNoticeDesk(
        store = store,
        validator = defaultInspectionNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionNoticeEngine(flags),
    )
}
