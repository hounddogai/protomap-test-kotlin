package com.acme.transport.customs.inspection.claim

import com.acme.transport.customs.clock.Clock

/** Moves inspection schedules through their life cycle and keeps a journal of what it did. */
data class InspectionScheduleCoordinator(
    var store: InspectionScheduleStore,
    var validator: InspectionScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("inspection schedule $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newInspectionScheduleCoordinator(store: InspectionScheduleStore, clock: Clock): InspectionScheduleCoordinator {
    return InspectionScheduleCoordinator(
        store = store,
        validator = defaultInspectionScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
