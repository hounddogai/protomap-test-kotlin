package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive inspection audits through their life cycle and keeps a journal of what it did. */
data class AdaptiveInspectionAuditManager(
    var store: AdaptiveInspectionAuditStore,
    var validator: AdaptiveInspectionAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive inspection audit $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveInspectionAuditStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive inspection audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveInspectionAuditStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newAdaptiveInspectionAuditManager(store: AdaptiveInspectionAuditStore, clock: Clock): AdaptiveInspectionAuditManager {
    return AdaptiveInspectionAuditManager(
        store = store,
        validator = defaultAdaptiveInspectionAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
