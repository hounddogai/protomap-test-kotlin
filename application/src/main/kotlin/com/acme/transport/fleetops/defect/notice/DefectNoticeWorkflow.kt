package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves defect notices through their life cycle and keeps a journal of what it did. */
data class DefectNoticeDesk(
    var store: DefectNoticeStore,
    var validator: DefectNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("defect notice $code not found")
        validator.requireValid(item)
        item.moveTo(DefectNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("defect notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("defect notice $code not found")
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

fun newDefectNoticeDesk(store: DefectNoticeStore, clock: Clock, flags: Registry): DefectNoticeDesk {
    return DefectNoticeDesk(
        store = store,
        validator = defaultDefectNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectNoticeEngine(flags),
    )
}
