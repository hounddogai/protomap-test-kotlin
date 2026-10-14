package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection audits through their life cycle and keeps a journal of what it did. */
data class InspectionAuditService(
    var store: InspectionAuditStore,
    var validator: InspectionAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection audit $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionAuditStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionAuditStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection audit $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newInspectionAuditService(store: InspectionAuditStore, clock: Clock): InspectionAuditService {
    return InspectionAuditService(
        store = store,
        validator = defaultInspectionAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionAuditEngine(),
    )
}
