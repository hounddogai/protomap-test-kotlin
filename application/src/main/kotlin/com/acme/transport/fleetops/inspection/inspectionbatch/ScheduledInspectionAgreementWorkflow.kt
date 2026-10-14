package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves scheduled inspection agreements through their life cycle and keeps a journal of what it did.
 */
data class ScheduledInspectionAgreementManager(
    var store: ScheduledInspectionAgreementStore,
    var validator: ScheduledInspectionAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled inspection agreement $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledInspectionAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled inspection agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledInspectionAgreementStatus.FAILED)
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

fun newScheduledInspectionAgreementManager(store: ScheduledInspectionAgreementStore, clock: Clock): ScheduledInspectionAgreementManager {
    return ScheduledInspectionAgreementManager(
        store = store,
        validator = defaultScheduledInspectionAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
