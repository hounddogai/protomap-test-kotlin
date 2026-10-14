package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.clock.Clock

/** Moves batched inspection schedules through their life cycle and keeps a journal of what it did. */
data class BatchedInspectionScheduleService(
    var store: BatchedInspectionScheduleStore,
    var validator: BatchedInspectionScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched inspection schedule $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedInspectionScheduleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched inspection schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedInspectionScheduleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newBatchedInspectionScheduleService(store: BatchedInspectionScheduleStore, clock: Clock): BatchedInspectionScheduleService {
    return BatchedInspectionScheduleService(
        store = store,
        validator = defaultBatchedInspectionScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
