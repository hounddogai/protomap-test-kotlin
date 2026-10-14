package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.crewing.clock.Clock

/** Moves duty period schedules through their life cycle and keeps a journal of what it did. */
data class DutyPeriodScheduleWorkflow(
    var store: DutyPeriodScheduleStore,
    var validator: DutyPeriodScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("duty period schedule $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodScheduleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodScheduleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDutyPeriodScheduleWorkflow(store: DutyPeriodScheduleStore, clock: Clock): DutyPeriodScheduleWorkflow {
    return DutyPeriodScheduleWorkflow(
        store = store,
        validator = defaultDutyPeriodScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
