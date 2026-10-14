package com.acme.transport.customs.clearance.assignment

import com.acme.transport.customs.clock.Clock

/** Moves pending clearance assignments through their life cycle and keeps a journal of what it did. */
data class PendingClearanceAssignmentCoordinator(
    var store: PendingClearanceAssignmentStore,
    var validator: PendingClearanceAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending clearance assignment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingClearanceAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending clearance assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingClearanceAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newPendingClearanceAssignmentCoordinator(store: PendingClearanceAssignmentStore, clock: Clock): PendingClearanceAssignmentCoordinator {
    return PendingClearanceAssignmentCoordinator(
        store = store,
        validator = defaultPendingClearanceAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
