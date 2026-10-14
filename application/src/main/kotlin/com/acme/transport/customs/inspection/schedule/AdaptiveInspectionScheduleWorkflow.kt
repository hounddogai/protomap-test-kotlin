package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.clock.Clock

/** Moves adaptive inspection schedules through their life cycle and keeps a journal of what it did. */
data class AdaptiveInspectionScheduleCoordinator(
    var store: AdaptiveInspectionScheduleStore,
    var validator: AdaptiveInspectionScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive inspection schedule $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveInspectionScheduleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive inspection schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveInspectionScheduleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAdaptiveInspectionScheduleCoordinator(store: AdaptiveInspectionScheduleStore, clock: Clock): AdaptiveInspectionScheduleCoordinator {
    return AdaptiveInspectionScheduleCoordinator(
        store = store,
        validator = defaultAdaptiveInspectionScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
