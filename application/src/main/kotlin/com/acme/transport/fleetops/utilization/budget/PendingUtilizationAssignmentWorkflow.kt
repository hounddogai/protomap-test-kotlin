package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves pending utilization assignments through their life cycle and keeps a journal of what it did.
 */
data class PendingUtilizationAssignmentWorkflow(
    var store: PendingUtilizationAssignmentStore,
    var validator: PendingUtilizationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending utilization assignment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingUtilizationAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending utilization assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingUtilizationAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newPendingUtilizationAssignmentWorkflow(store: PendingUtilizationAssignmentStore, clock: Clock): PendingUtilizationAssignmentWorkflow {
    return PendingUtilizationAssignmentWorkflow(
        store = store,
        validator = defaultPendingUtilizationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
