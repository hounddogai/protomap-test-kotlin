package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection schedules through their life cycle and keeps a journal of what it did. */
data class InspectionScheduleService(
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
        item.moveTo(InspectionScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newInspectionScheduleService(store: InspectionScheduleStore, clock: Clock): InspectionScheduleService {
    return InspectionScheduleService(
        store = store,
        validator = defaultInspectionScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
