package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.clock.Clock

/** Moves tiered defect audits through their life cycle and keeps a journal of what it did. */
data class TieredDefectAuditWorkflow(
    var store: TieredDefectAuditStore,
    var validator: TieredDefectAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered defect audit $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDefectAuditStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered defect audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDefectAuditStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTieredDefectAuditWorkflow(store: TieredDefectAuditStore, clock: Clock): TieredDefectAuditWorkflow {
    return TieredDefectAuditWorkflow(
        store = store,
        validator = defaultTieredDefectAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
