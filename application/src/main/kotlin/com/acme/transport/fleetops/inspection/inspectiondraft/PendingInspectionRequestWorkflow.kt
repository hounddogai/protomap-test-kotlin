package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.clock.Clock

/** Moves pending inspection requests through their life cycle and keeps a journal of what it did. */
data class PendingInspectionRequestDesk(
    var store: PendingInspectionRequestStore,
    var validator: PendingInspectionRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending inspection request $code not found")
        validator.requireValid(item)
        item.moveTo(PendingInspectionRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending inspection request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingInspectionRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPendingInspectionRequestDesk(store: PendingInspectionRequestStore, clock: Clock): PendingInspectionRequestDesk {
    return PendingInspectionRequestDesk(
        store = store,
        validator = defaultPendingInspectionRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
